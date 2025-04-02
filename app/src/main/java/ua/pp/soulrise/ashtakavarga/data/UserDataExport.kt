package ua.pp.soulrise.ashtakavarga.data

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

data class UserDataExport(
    @Expose(serialize = false, deserialize = false)
    val user: UserEntity,
    @Expose(serialize = false, deserialize = false)
    val transits: List<TransitEntity>,
    @Expose(serialize = false, deserialize = false)
    val positions: List<PlanetaryPositionEntity>,
    @Expose(serialize = false, deserialize = false)
    val selections: List<PlanetSignSelectionEntity>
)