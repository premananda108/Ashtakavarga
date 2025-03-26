package ua.pp.soulrise.ashtakavarga.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ua.pp.soulrise.ashtakavarga.common.Planet

@Dao
interface AstrologyDao {

    // --- PlanetaryPositions ---

    @Query("SELECT * FROM planetary_positions WHERE planet_id = :planetId AND sign_id = :signId LIMIT 1")
    suspend fun getPlanetaryPosition(planetId: Int, signId: Int): PlanetaryPositionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Игнорируем вставку, если уже есть (для upsert)
    suspend fun insertPlanetaryPosition(entity: PlanetaryPositionEntity): Long

    @Update
    suspend fun updatePlanetaryPosition(entity: PlanetaryPositionEntity): Int

    // Метод Upsert (вставить или обновить) для PlanetaryPosition
    @Transaction
    suspend fun upsertPlanetaryPosition(planetId: Int, signId: Int, value: Int?) {
        val existing = getPlanetaryPosition(planetId, signId)
        if (existing != null) {
            // Обновляем только если значение изменилось или было null/стало null
            if (existing.value != value) {
                updatePlanetaryPosition(existing.copy(value = value))
            }
        } else {
            insertPlanetaryPosition(PlanetaryPositionEntity(planetId = planetId, signId = signId, value = value))
        }
    }

    @Query("SELECT * FROM planetary_positions")
    fun getAllPlanetaryPositions(): Flow<List<PlanetaryPositionEntity>> // Используем Flow для наблюдения

    @Query("SELECT value FROM planetary_positions WHERE planet_id = :housePlanetId AND sign_id = :signId LIMIT 1")
    suspend fun getHomeValue(signId: Int, housePlanetId: Int = Planet.HOUSE): Int?

    // --- PlanetSignSelections ---

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Заменяем при конфликте planet_id
    suspend fun savePlanetSignSelection(selection: PlanetSignSelectionEntity)

    @Query("SELECT * FROM planet_sign_selections WHERE planet_id = :planetId LIMIT 1")
    suspend fun getPlanetSignSelection(planetId: Int): PlanetSignSelectionEntity?

    // --- Transits ---

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Заменяем при конфликте planet_id
    suspend fun saveTransit(transit: TransitEntity)

    @Query("SELECT * FROM transits WHERE planet_id = :planetId LIMIT 1")
    suspend fun getTransit(planetId: Int): TransitEntity?
}