package ua.pp.soulrise.ashtakavarga.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import com.google.gson.Gson

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM transits WHERE user_id = :userId")
    suspend fun deleteUserTransits(userId: Long)

    @Query("DELETE FROM planet_sign_selections WHERE user_id = :userId")
    suspend fun deletePlanetSignSelections(userId: Long)

    @Query("DELETE FROM planetary_positions WHERE user_id = :userId")
    suspend fun deletePlanetaryPositions(userId: Long)

    @Transaction
    suspend fun deleteUserWithTable(user: UserEntity) {
        deleteUserTransits(user.userId)
        deletePlanetSignSelections(user.userId)
        deletePlanetaryPositions(user.userId)
        delete(user)
    }

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?
    
    @Transaction
    suspend fun exportUserData(userId: Long, astrologyDao: AstrologyDao): String {
        val user = getUserById(userId) ?: return ""
        val transits = astrologyDao.getTransitsByUserId(userId)
        val positions = astrologyDao.getPlanetaryPositionsByUserId(userId)
        val selections = astrologyDao.getPlanetSignSelectionsByUserId(userId)
        
        val exportData = UserDataExport(
            user = UserExportData(
                name = user.name,
                dateOfBirth = user.dateOfBirth,
                timeOfBirth = user.timeOfBirth,
                birthPlace = user.birthPlace
            ),
            transits = transits.map { TransitExportData(planetId = it.planetId, signId = it.signId) },
            positions = positions.map { PlanetaryPositionExportData(planetId = it.planetId, signId = it.signId, value = it.value) },
            selections = selections.map { PlanetSignSelectionExportData(planetId = it.planetId, signId = it.signId) }
        )
        
        return Gson().toJson(exportData)
    }

    @Transaction
    suspend fun importUserData(jsonData: String, astrologyDao: AstrologyDao) {
        try {
            val data = Gson().fromJson(jsonData, UserDataExport::class.java)
            
            // Создаем нового пользователя с автоматически сгенерированным ID
            val newUser = UserEntity(
                name = data.user.name,
                dateOfBirth = data.user.dateOfBirth,
                timeOfBirth = data.user.timeOfBirth,
                birthPlace = data.user.birthPlace
            )
            val newUserId = insert(newUser)
            
            // Вставляем связанные данные с новым ID пользователя
            data.transits.forEach { transit ->
                astrologyDao.insertTransit(TransitEntity(
                    planetId = transit.planetId,
                    signId = transit.signId,
                    userId = newUserId
                ))
            }
            
            data.positions.forEach { position ->
                astrologyDao.insertPlanetaryPosition(PlanetaryPositionEntity(
                    planetId = position.planetId,
                    signId = position.signId,
                    value = position.value,
                    userId = newUserId
                ))
            }
            
            data.selections.forEach { selection ->
                astrologyDao.insertPlanetSignSelection(PlanetSignSelectionEntity(
                    planetId = selection.planetId,
                    signId = selection.signId,
                    userId = newUserId
                ))
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Ошибка при импорте данных: ${e.message}")
        }
    }
}