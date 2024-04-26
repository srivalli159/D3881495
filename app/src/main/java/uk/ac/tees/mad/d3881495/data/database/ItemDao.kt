package uk.ac.tees.mad.d3881495.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorite(favoriteEntity: LikedItemEntity)

    @Query("select * from LikedItemEntity")
    fun getAllFavorite(): Flow<List<LikedItemEntity>>

    @Delete
    suspend fun deleteFromFavorite(favoriteEntity: LikedItemEntity)

}