package ua.pp.soulrise.ashtakavarga.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "planet_sign_selections",
    // Уникальный индекс по planet_id
    indices = [
        Index(value = ["user_id", "planet_id"], unique = true)
    ]
)
data class PlanetSignSelectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "planet_id")
    val planetId: Int,

    @ColumnInfo(name = "sign_id")
    val signId: Int,

    @ColumnInfo(name = "user_id")
    val userId: Long
)