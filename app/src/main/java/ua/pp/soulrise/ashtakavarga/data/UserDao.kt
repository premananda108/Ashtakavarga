package ua.pp.soulrise.ashtakavarga.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction

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
}