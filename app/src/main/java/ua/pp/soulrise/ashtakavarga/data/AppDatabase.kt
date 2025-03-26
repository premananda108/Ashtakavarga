package ua.pp.soulrise.ashtakavarga.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlanetaryPositionEntity::class,
        PlanetSignSelectionEntity::class,
        TransitEntity::class
    ],
    version = 1, // Начинаем с версии 1 для Room. Если схема изменится, увеличивайте.
    exportSchema = false // Отключаем экспорт схемы для простоты
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun astrologyDao(): AstrologyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "astrology_database_room" // Новое имя файла БД
                )
                    // .addMigrations(...) // Добавьте миграции при изменении схемы в будущем
                    // .fallbackToDestructiveMigration() // Или используйте это для очистки при обновлении версии (ТЕРЯЕТ ДАННЫЕ!)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}