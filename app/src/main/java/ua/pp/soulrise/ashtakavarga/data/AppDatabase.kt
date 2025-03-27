package ua.pp.soulrise.ashtakavarga.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlanetaryPositionEntity::class,
        PlanetSignSelectionEntity::class,
        TransitEntity::class,
        UserEntity::class // <--- Добавьте UserEntity
    ],
    version = 3, // Увеличена версия для поддержки user_id в planetary_positions
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun astrologyDao(): AstrologyDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "astrology_database_room" // Новое имя файла БД для миграции
                )
                    //.fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}