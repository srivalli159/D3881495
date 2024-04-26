package uk.ac.tees.mad.d3881495.data.database

import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.d3881495.domain.Item
import uk.ac.tees.mad.d3881495.domain.ItemResponse

interface RoomRepository {

    suspend fun addToLiked(item: String)

    fun getAllLiked(): Flow<List<LikedItemEntity>>

    suspend fun deleteFromLikedItems(item: ItemResponse)

}

class RoomRepositoryImpl(
    private val dao: ItemDao
) : RoomRepository {

    override suspend fun addToLiked(item: String) {
        dao.addToFavorite(LikedItemEntity(item))
    }

    override fun getAllLiked(): Flow<List<LikedItemEntity>> = dao.getAllFavorite()

    override suspend fun deleteFromLikedItems(item: ItemResponse) =
        dao.deleteFromFavorite(item.toLikedEntity())

}