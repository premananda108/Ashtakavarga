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
        deleteUserTransits(user.userId.toLong())
        deletePlanetSignSelections(user.userId.toLong())
        deletePlanetaryPositions(user.userId.toLong())
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
        
        return Gson().toJson(UserDataExport(
            user = user,
            transits = transits,
            positions = positions,
            selections = selections
        ))
    }

    @Transaction
    suspend fun importUserData(jsonData: String, astrologyDao: AstrologyDao) {
        try {
            val data = Gson().fromJson(jsonData, UserDataExport::class.java)
            
            // Создаем нового пользователя с новым ID
            val newUser = data.user.copy(userId = 0)
            val newUserId = insert(newUser)
            
            // Вставляем связанные данные с новым ID пользователя
            data.transits.forEach { transit ->
                astrologyDao.insertTransit(transit.copy(userId = newUserId))
            }
            
            data.positions.forEach { position ->
                astrologyDao.insertPlanetaryPosition(position.copy(userId = newUserId))
            }
            
            data.selections.forEach { selection ->
                astrologyDao.insertPlanetSignSelection(selection.copy(userId = newUserId))
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Ошибка при импорте данных: ${e.message}")
        }
    }
}