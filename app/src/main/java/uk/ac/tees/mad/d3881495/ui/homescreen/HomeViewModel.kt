package uk.ac.tees.mad.d3881495.ui.homescreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.data.database.RoomRepository
import uk.ac.tees.mad.d3881495.data.repository.FirestoreDatabaseRepository
import uk.ac.tees.mad.d3881495.domain.ItemResponse
import uk.ac.tees.mad.d3881495.domain.Resource
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestoreRepository: FirestoreDatabaseRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _itemsList = Channel<RetrievedItemState>()
    val itemsList = _itemsList.receiveAsFlow()

    init {
        getSportsItemList()
    }


    fun getSportsItemList() = viewModelScope.launch {
        firestoreRepository.fetchAllSportItems().collect {
            when (it) {
                is Resource.Error -> {
                    _itemsList.send(RetrievedItemState(isError = it.message))
                }

                is Resource.Loading -> {
                    _itemsList.send(RetrievedItemState(isLoading = true))

                }

                is Resource.Success -> {
                    _itemsList.send(RetrievedItemState(isSuccess = it.data))
                }
            }
        }
    }

    fun addItemToFavorite(item: String) = viewModelScope.launch {
        roomRepository.addToLiked(item)
    }
}

data class RetrievedItemState(
    val isLoading: Boolean = false,
    val isSuccess: List<ItemResponse>? = null,
    val isError: String? = null
)