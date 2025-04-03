package ua.pp.soulrise.ashtakavarga.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "planetary_positions",
    // Індекси для прискорення запитів
    indices = [
        Index(value = ["user_id", "planet_id", "sign_id"], unique = true) // Комбінований індекс
    ]
)
data class PlanetaryPositionEntity(
    @PrimaryKey(autoGenerate = true) // Додано id як первинний ключ з автогенерацією
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long = 0,

    @ColumnInfo(name = "planet_id")
    val planetId: Int,

    @ColumnInfo(name = "sign_id")
    val signId: Int,

    @ColumnInfo(name = "value")
    val value: Int?
)