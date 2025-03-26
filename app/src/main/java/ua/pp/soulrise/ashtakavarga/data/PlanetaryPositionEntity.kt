package ua.pp.soulrise.ashtakavarga.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "planetary_positions",
    // Индексы для ускорения запросов по planet_id и sign_id
    indices = [Index(value = ["planet_id", "sign_id"], unique = true)]
)
data class PlanetaryPositionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "planet_id")
    val planetId: Int,

    @ColumnInfo(name = "sign_id")
    val signId: Int,

    @ColumnInfo(name = "value")
    val value: Int? // Разрешаем NULL
)