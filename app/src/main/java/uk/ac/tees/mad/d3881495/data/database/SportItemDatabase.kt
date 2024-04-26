package uk.ac.tees.mad.d3881495.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LikedItemEntity::class], version = 1, exportSchema = false)
abstract class SportItemDatabase : RoomDatabase() {
    abstract fun getDao(): ItemDao
}