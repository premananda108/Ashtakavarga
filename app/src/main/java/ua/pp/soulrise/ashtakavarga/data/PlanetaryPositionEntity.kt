package ua.pp.soulrise.ashtakavarga.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "planetary_positions",
    // Индексы для ускорения запросов по planet_id, sign_id и user_id
    indices = [Index(value = ["planet_id", "sign_id", "user_id"], unique = true)]
)
data class PlanetaryPositionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "planet_id")
    val planetId: Int,

    @ColumnInfo(name = "sign_id")
    val signId: Int,

    @ColumnInfo(name = "value")
    val value: Int? // Разрешаем NULL
)