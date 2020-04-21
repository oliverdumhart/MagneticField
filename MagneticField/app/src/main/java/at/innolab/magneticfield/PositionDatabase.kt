package at.innolab.magneticfield

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Position::class], version = 1, exportSchema = false)
abstract class PositionDatabase : RoomDatabase() {

    abstract val positionDatabaseDao: PositionDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: PositionDatabase? = null

        fun getInstance(context: Context): PositionDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            PositionDatabase::class.java,
                            "position_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}