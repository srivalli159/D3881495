package uk.ac.tees.mad.d3881495.ui.itemDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.data.repository.FirestoreDatabaseRepository
import uk.ac.tees.mad.d3881495.domain.ItemResponse
import uk.ac.tees.mad.d3881495.domain.Resource
import javax.inject.Inject


@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestoreRepository: FirestoreDatabaseRepository
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle[ItemDetailsRoute.itemIdArg])

    private val _itemDetail = Channel<RetrievedItemState>()
    val itemDetail = _itemDetail.receiveAsFlow()


    init {
        fetchItemDetailsById(itemId)
    }

    private fun fetchItemDetailsById(key: String) = viewModelScope.launch {
        firestoreRepository.fetchItemDetailsById(key).collect {
            when (it) {
                is Resource.Error -> {
                    _itemDetail.send(RetrievedItemState(isError = it.message))
                }

                is Resource.Loading -> {
                    _itemDetail.send(RetrievedItemState(isLoading = true))
                }

                is Resource.Success -> {
                    _itemDetail.send(RetrievedItemState(isSuccess = it.data))
                }
            }
        }
    }

}

data class RetrievedItemState(
    val isLoading: Boolean = false,
    val isSuccess: ItemResponse? = null,
    val isError: String? = null
)