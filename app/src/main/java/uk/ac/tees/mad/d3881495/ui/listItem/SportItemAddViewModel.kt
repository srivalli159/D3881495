package uk.ac.tees.mad.d3881495.ui.listItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.data.repository.FirestoreDatabaseRepository
import uk.ac.tees.mad.d3881495.domain.Item
import uk.ac.tees.mad.d3881495.domain.Resource
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SportItemAddViewModel @Inject constructor(
    private val sportsItemRepository: FirestoreDatabaseRepository,
    private val userAuth: FirebaseAuth
) : ViewModel() {


    private val _addItemUiState = MutableStateFlow(Item())
    val addItemUiState = _addItemUiState.asStateFlow()

    private val _addSportItemState = Channel<SportItemAdditionState>()
    val addSportItemState = _addSportItemState.receiveAsFlow()

    fun modifyViewState(updatedState: Item) {
        _addItemUiState.value = updatedState
    }

    fun clearViewState() {
        _addItemUiState.value = Item()
    }

    fun submitSportItem() = viewModelScope.launch {
        val timeStampNow = Date()

        modifyViewState(
            _addItemUiState.value.copy(
                listedDate = timeStampNow,
                listedByKey = userAuth.currentUser?.uid.orEmpty()
            )
        )

        sportsItemRepository.uploadSportItemDetails(_addItemUiState.value).collect { result ->
            when (result) {
                is Resource.Loading -> _addSportItemState.send(SportItemAdditionState(isLoading = true))
                is Resource.Success -> _addSportItemState.send(SportItemAdditionState(data = result.data))
                is Resource.Error -> _addSportItemState.send(SportItemAdditionState(error = result.message))
            }
        }
    }
}


data class SportItemAdditionState(
    val data: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)