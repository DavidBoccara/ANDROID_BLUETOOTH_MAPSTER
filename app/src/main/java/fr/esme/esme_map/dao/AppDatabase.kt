package fr.esme.esme_map.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.esme.esme_map.model.Category
import fr.esme.esme_map.model.POI
import fr.esme.esme_map.model.Position
import fr.esme.esme_map.model.User

@Database(entities = [POI::class, User::class, Category::class, Position::class], version = 1)
@TypeConverters(
    Converters::class
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun poiDao(): POIDao
}