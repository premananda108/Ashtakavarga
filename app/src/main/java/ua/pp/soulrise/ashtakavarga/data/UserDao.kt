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
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM transits WHERE user_id = :userId")
    suspend fun deleteUserTransits(userId: Int)

    @Query("DELETE FROM planet_sign_selections WHERE user_id = :userId")
    suspend fun deletePlanetSignSelections(userId: Int)

    @Query("DELETE FROM planetary_positions WHERE user_id = :userId")
    suspend fun deletePlanetaryPositions(userId: Int)

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
    suspend fun getUserById(userId: Int): UserEntity?
    
    @Transaction
    suspend fun exportUserData(userId: Int, astrologyDao: AstrologyDao): String {
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
            
            // Вставляем пользователя
            insert(data.user)
            
            // Вставляем связанные данные
            data.transits.forEach { transit ->
                astrologyDao.insertTransit(transit)
            }
            
            data.positions.forEach { position ->
                astrologyDao.insertPlanetaryPosition(position)
            }
            
            data.selections.forEach { selection ->
                astrologyDao.insertPlanetSignSelection(selection)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Ошибка при импорте данных: ${e.message}")
        }
    }
}