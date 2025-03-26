package ua.pp.soulrise.ashtakavarga.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: Long // Храним дату как timestamp (количество миллисекунд с 1970)
)