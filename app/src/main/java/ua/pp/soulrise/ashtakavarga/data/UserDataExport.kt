package ua.pp.soulrise.ashtakavarga.data

import com.google.gson.annotations.SerializedName

data class UserDataExport(
    val user: UserExportData,
    val transits: List<TransitExportData>,
    val positions: List<PlanetaryPositionExportData>,
    val selections: List<PlanetSignSelectionExportData>
)

data class UserExportData(
    val name: String,
    val dateOfBirth: Long,
    val timeOfBirth: Long,
    val birthPlace: String
)

data class TransitExportData(
    val planetId: Int,
    val signId: Int
)

data class PlanetaryPositionExportData(
    val planetId: Int,
    val signId: Int,
    val value: Int?
)

data class PlanetSignSelectionExportData(
    val planetId: Int,
    val signId: Int
)