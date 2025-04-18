package ua.pp.soulrise.ashtakavarga.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: Long, // Храним дату как timestamp (количество миллисекунд с 1970)

    @ColumnInfo(name = "time_of_birth")
    val timeOfBirth: Long = 0, // Время рождения в миллисекундах

    @ColumnInfo(name = "birth_place")
    val birthPlace: String = "" // Место рождения
)