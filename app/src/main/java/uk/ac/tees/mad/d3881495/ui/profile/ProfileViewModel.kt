package uk.ac.tees.mad.d3881495.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.data.repository.AuthRepository
import uk.ac.tees.mad.d3881495.domain.Resource
import uk.ac.tees.mad.d3881495.domain.UserData
import uk.ac.tees.mad.d3881495.domain.UserDataState
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _getUserState = Channel<UserResult>()
    val getUserState = _getUserState.receiveAsFlow()

    private val _updateUserState = Channel<UserUpdateResult>()
    val updateUserState = _updateUserState.receiveAsFlow()

    private val _uiState = MutableStateFlow(UserData())
    val uiState = _uiState.asStateFlow()

    fun updateUserDataState(state: UserData) {
        _uiState.value = state
    }

    init {
        getUserInformation()
    }

    fun getUserInformation() = viewModelScope.launch {
        authRepository.getCurrentUser().collect {
            when (it) {
                is Resource.Error -> {
                    _getUserState.send(UserResult(errorMessage = it.message))
                }

                is Resource.Loading -> {
                    _getUserState.send(UserResult(isSuccessful = true))
                }

                is Resource.Success -> {
                    _getUserState.send(UserResult(data = it.data))

                }
            }
        }
    }

    fun updateProfile() = viewModelScope.launch {
        authRepository.updateCurrentUser(_uiState.value).collect {
            when (it) {
                is Resource.Error -> {
                    _updateUserState.send(UserUpdateResult(errorMessage = it.message))
                }

                is Resource.Loading -> {
                    _updateUserState.send(UserUpdateResult(isLoading = true))
                }

                is Resource.Success -> {
                    _updateUserState.send(UserUpdateResult(data = it.data))
                }
            }
        }
    }


    fun processSelectedImage(uri: Uri, context: Context) {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images
                .Media
                .getBitmap(context.contentResolver, uri)

        } else {
            val source = ImageDecoder
                .createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        val imageByteArray = convertBitmapToByteArray(bitmap)
        _uiState.update {
            it.copy(image = imageByteArray)
        }
    }

    fun processCapturedImage(bitmap: Bitmap) {
        val imageByteArray = convertBitmapToByteArray(bitmap)
        _uiState.update {
            it.copy(image = imageByteArray)
        }
    }

}

fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

fun getImageFromUrl(url: String): ByteArray? {
    try {
        val imageUrl = URL(url)
        val connection = imageUrl.openConnection()
        val inputStream = connection.getInputStream()
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var read = 0
        while (inputStream.read(buffer, 0, buffer.size).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        return outputStream.toByteArray()
    } catch (e: Exception) {
        Log.d("ImageManager", "Error: $e")
    }
    return null
}

data class UserResult(
    val data: UserDataState? = null,
    val isSuccessful: Boolean = false,
    val errorMessage: String? = null
)

data class UserUpdateResult(
    val data: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)