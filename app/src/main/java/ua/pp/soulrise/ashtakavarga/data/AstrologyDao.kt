package ua.pp.soulrise.ashtakavarga.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.lifecycle.LiveData
import ua.pp.soulrise.ashtakavarga.common.Planet

@Dao
interface AstrologyDao {
    @Query("SELECT * FROM transits WHERE user_id = :userId")
    suspend fun getTransitsByUserId(userId: Long): List<TransitEntity>

    @Query("SELECT * FROM planetary_positions WHERE user_id = :userId")
    suspend fun getPlanetaryPositionsByUserId(userId: Long): List<PlanetaryPositionEntity>

    @Query("SELECT * FROM planet_sign_selections WHERE user_id = :userId")
    suspend fun getPlanetSignSelectionsByUserId(userId: Long): List<PlanetSignSelectionEntity>
    @Query("SELECT * FROM planetary_positions WHERE planet_id = :planetId AND sign_id = :signId AND user_id = :userId")
    suspend fun getPlanetaryPosition(planetId: Int, signId: Int, userId: kotlin.Long): PlanetaryPositionEntity?

    @Insert
    suspend fun insertPlanetaryPosition(entity: PlanetaryPositionEntity): Long

    @Update
    suspend fun updatePlanetaryPosition(entity: PlanetaryPositionEntity): Int

    @Transaction
    suspend fun upsertPlanetaryPosition(planetId: Int, signId: Int, userId: Long, value: Int?) {
        val existing = getPlanetaryPosition(planetId, signId, userId)
        if (existing != null) {
            // Обновляем только если значение изменилось или было null/стало null
            if (existing.value != value) {
                updatePlanetaryPosition(existing.copy(value = value))
            }
        } else {
            insertPlanetaryPosition(PlanetaryPositionEntity(planetId = planetId, signId = signId, userId = userId, value = value))
        }
    }

    @Query("SELECT * FROM planetary_positions WHERE user_id = :userId")
    fun getAllPlanetaryPositionsLiveData(userId: Long): LiveData<List<PlanetaryPositionEntity>> // Используем LiveData для наблюдения

    @Query("SELECT value FROM planetary_positions WHERE planet_id = :housePlanetId AND sign_id = :signId AND user_id = :userId LIMIT 1")
    suspend fun getHomeValue(signId: Int, userId: Long, housePlanetId: Int = Planet.HOUSE): Int?

    // --- PlanetSignSelections ---

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Заменяем при конфликте planet_id
    suspend fun savePlanetSignSelection(selection: PlanetSignSelectionEntity)

    @Query("SELECT * FROM planet_sign_selections WHERE planet_id = :planetId AND user_id = :userId LIMIT 1")
    suspend fun getPlanetSignSelection(planetId: Int, userId: kotlin.Long): PlanetSignSelectionEntity?

    // --- Transits ---

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Заменяем при конфликте planet_id
    suspend fun saveTransit(transit: TransitEntity)

    @Query("SELECT * FROM transits WHERE planet_id = :planetId AND user_id = :userId LIMIT 1")
    suspend fun getTransit(planetId: Int, userId: kotlin.Long): TransitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransit(transit: TransitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanetSignSelection(selection: PlanetSignSelectionEntity)
    
    // === НОВЫЕ МЕТОДЫ ДЛЯ ПАКЕТНЫХ ОПЕРАЦИЙ ===
    
    @Insert
    suspend fun insertPlanetaryPositions(entities: List<PlanetaryPositionEntity>): List<Long>
    
    @Update
    suspend fun updatePlanetaryPositions(entities: List<PlanetaryPositionEntity>): Int
    
    @Query("SELECT * FROM planetary_positions WHERE user_id = :userId")
    suspend fun getAllPlanetaryPositions(userId: Long): List<PlanetaryPositionEntity>
    
    @Transaction
    suspend fun upsertPlanetaryPositions(positions: List<PlanetaryPositionEntity>) {
        // Группируем все позиции по пользователю для оптимизации запросов
        val positionsByUser = positions.groupBy { it.userId }
        
        positionsByUser.forEach { (userId, userPositions) ->
            // Получаем все существующие позиции для данного пользователя за один запрос
            val existingPositions = getAllPlanetaryPositions(userId)
            val existingMap = existingPositions.associateBy { Pair(it.planetId, it.signId) }
            
            // Разделяем на обновления и вставки
            val toUpdate = mutableListOf<PlanetaryPositionEntity>()
            val toInsert = mutableListOf<PlanetaryPositionEntity>()
            
            userPositions.forEach { position ->
                val key = Pair(position.planetId, position.signId)
                val existing = existingMap[key]
                
                if (existing != null) {
                    // Обновляем только если значение изменилось
                    if (existing.value != position.value) {
                        toUpdate.add(position.copy(id = existing.id))
                    }
                } else {
                    toInsert.add(position)
                }
            }
            
            // Выполняем массовые операции
            if (toInsert.isNotEmpty()) {
                insertPlanetaryPositions(toInsert)
            }
            if (toUpdate.isNotEmpty()) {
                updatePlanetaryPositions(toUpdate)
            }
        }
    }
}