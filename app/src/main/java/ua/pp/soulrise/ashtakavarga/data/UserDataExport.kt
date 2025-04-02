package ua.pp.soulrise.ashtakavarga.data

data class UserDataExport(
    val user: UserEntity,
    val transits: List<TransitEntity>,
    val positions: List<PlanetaryPositionEntity>,
    val selections: List<PlanetSignSelectionEntity>
)