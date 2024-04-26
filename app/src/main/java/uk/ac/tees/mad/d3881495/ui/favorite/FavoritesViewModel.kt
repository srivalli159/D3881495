package uk.ac.tees.mad.d3881495.ui.favorite

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.data.database.RoomRepository
import uk.ac.tees.mad.d3881495.data.repository.DatabaseRepository
import uk.ac.tees.mad.d3881495.data.repository.FirestoreDatabaseRepository
import uk.ac.tees.mad.d3881495.domain.ItemResponse
import uk.ac.tees.mad.d3881495.domain.Resource
import uk.ac.tees.mad.d3881495.ui.homescreen.RetrievedItemState
import javax.inject.Inject


@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val firestoreRepository: FirestoreDatabaseRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _favoriteItemListState = Channel<RetrievedItemState>()
    val favoriteItemListState = _favoriteItemListState.receiveAsFlow()

    var favoriteItem by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        loadAllLikedItems()
    }

    fun loadAllLikedItems() {
        viewModelScope.launch {
            roomRepository
                .getAllLiked().onEach {
                    favoriteItem = it.map { item -> item.itemId }
                    getLikedItemList()
                }.launchIn(viewModelScope)
        }
    }

    private fun getLikedItemList() = viewModelScope.launch {
        firestoreRepository.getMultipleItemsWithKeys(favoriteItem).collect {
            when (it) {
                is Resource.Error -> {
                    _favoriteItemListState.send(RetrievedItemState(isError = it.message))
                }

                is Resource.Loading -> {
                    _favoriteItemListState.send(RetrievedItemState(isLoading = true))

                }

                is Resource.Success -> {
                    _favoriteItemListState.send(RetrievedItemState(isSuccess = it.data))
                }
            }
        }
    }

    fun deleteFromFavorite(item: ItemResponse, context: Context) = viewModelScope.launch {
        roomRepository.deleteFromLikedItems(item)
    }.invokeOnCompletion {
        Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
        loadAllLikedItems()
    }
}