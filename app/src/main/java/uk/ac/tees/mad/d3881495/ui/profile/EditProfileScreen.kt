package uk.ac.tees.mad.d3881495.ui.profile

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.NavigationRoute
import uk.ac.tees.mad.d3881495.R
import uk.ac.tees.mad.d3881495.domain.UserData
import uk.ac.tees.mad.d3881495.utils.ApplicationViewModel
import uk.ac.tees.mad.d3881495.utils.LocationRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditProfile(
    viewModel: ProfileViewModel,
    getUserState: UserResult?,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateUserState by viewModel.updateUserState.collectAsState(initial = null)
    val appContext = LocalContext.current
    val focusControl = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var displayBottomSheet by remember { mutableStateOf(false) }
    val stateBottomSheet = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(Unit) {
        viewModel.getUserInformation()
    }
    LaunchedEffect(getUserState) {
        getUserState?.errorMessage?.let {
            println("item.... $it")

        }
        getUserState?.data?.let {
            println("item.... $it")
            val item = it
            launch(Dispatchers.IO) {
                viewModel.updateUserDataState(
                    UserData(
                        name = item.name,
                        email = item.email,
                        image = getImageFromUrl(item.image ?: ""),
                        address = item.address,
                    )
                )
            }
        }
    }

    // Image selection from gallery
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val pickImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
            uri?.let { viewModel.processSelectedImage(it, appContext) }
        }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let { viewModel.processCapturedImage(it) }
        }

    val permissionCamera = rememberPermissionState(Manifest.permission.CAMERA)
    val appViewModel: ApplicationViewModel = hiltViewModel()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray.copy(alpha = 0.05f))
            .verticalScroll(rememberScrollState())
    ) {


        if (displayBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { displayBottomSheet = false },
                sheetState = stateBottomSheet,
                windowInsets = WindowInsets.ime
            ) {
                // Bottom sheet content
                ImageSourceSelector(chooseGallery = {
                    scope.launch { stateBottomSheet.hide() }.invokeOnCompletion {
                        if (!stateBottomSheet.isVisible) {
                            displayBottomSheet = false
                        }
                    }
                    pickImageLauncher.launch("image/*")
                }, chooseCamera = {
                    scope.launch { stateBottomSheet.hide() }.invokeOnCompletion {
                        if (!stateBottomSheet.isVisible) {
                            displayBottomSheet = false
                        }
                    }
                    if (permissionCamera.status.isGranted) {
                        takePictureLauncher.launch(null)
                    } else {
                        permissionCamera.launchPermissionRequest()
                        if (permissionCamera.status.isGranted) {
                            takePictureLauncher.launch(null)
                        }
                    }
                })
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onComplete) {
                    Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = null)
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                    Text(text = "Edit profile", fontSize = 19.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            PhotoUploadButton(
                onClick = { displayBottomSheet = true }, photo = uiState.image
            )
            Spacer(modifier = Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InputField(
                    label = "Name",
                    value = uiState.name,
                    onValueChange = { viewModel.updateUserDataState(uiState.copy(name = it)) },
                    focusManager = focusControl
                )
                AddressInputField(
                    label = "Address",
                    value = uiState.address,
                    onValueChange = { viewModel.updateUserDataState(uiState.copy(address = it)) },
                    focusManager = focusControl,
                    locationManager = LocationRepository(
                        context = appContext, activity = (appContext as Activity)
                    ),
                    applicationViewModel = appViewModel
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.updateProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (updateUserState?.isLoading == true) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(text = "Save Profile")
                }
            }
        }
    }
    LaunchedEffect(key1 = updateUserState) {
        updateUserState?.data?.let {
            Toast.makeText(appContext, it, Toast.LENGTH_SHORT).show()
            onComplete()
        }
        updateUserState?.errorMessage?.let {
            Toast.makeText(appContext, it, Toast.LENGTH_SHORT).show()
        }
    }
}


@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusManager: FocusManager,
    isLast: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        Text(text = label)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(),
            placeholder = { Text(text = label) },
            keyboardOptions = KeyboardOptions(imeAction = if (isLast) ImeAction.Done else ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }),
            trailingIcon = trailingIcon
        )
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddressInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusManager: FocusManager,
    locationManager: LocationRepository,
    applicationViewModel: ApplicationViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    val gpsEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        gpsEnabled.value = locationManager.gpsStatus.first()
    }

    InputField(label = label,
        value = value,
        onValueChange = onValueChange,
        focusManager = focusManager,
        trailingIcon = {
            IconButton(onClick = {
                if (permissionState.status.isGranted) {
                    if (gpsEnabled.value) {
                        // Fetch current location and address
                        coroutineScope.launch {
                            applicationViewModel.locationFlow.collectLatest { location ->
                                val address = locationManager.getAddressFromCoordinate(
                                    location.latitude, location.longitude
                                )
                                println(address)
                                onValueChange(address)
                            }
                        }
                    } else {
                        // Prompt user to enable GPS
                        Toast.makeText(context, "Please enable GPS", Toast.LENGTH_SHORT).show()
                        locationManager.checkGpsSettings()
                    }
                } else {
                    permissionState.launchPermissionRequest()
                }
            }) {
                Icon(Icons.Default.LocationOn, contentDescription = "Fetch Location")
            }
        })
}

@Composable
fun PhotoUploadButton(
    onClick: () -> Unit, photo: ByteArray?
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .height(100.dp)
            .aspectRatio(1f),
        shape = CircleShape,
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (photo == null) {
                Icon(
                    imageVector = Icons.Rounded.AddAPhoto,
                    contentDescription = "Add image",
                    modifier = Modifier.size(40.dp)
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).crossfade(true).data(photo)
                        .build(),
                    contentDescription = "Profile image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
}

@Composable
fun ImageSourceSelector(
    chooseCamera: () -> Unit, chooseGallery: () -> Unit
) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .clickable { chooseCamera() }
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CameraEnhance,
                contentDescription = "Select from camera",
                modifier = Modifier
                    .padding(16.dp)
                    .size(35.dp)
            )

            Text(
                text = "Camera", modifier = Modifier.padding(16.dp), fontSize = 16.sp
            )
        }
        Row(modifier = Modifier
            .clickable { chooseGallery() }
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Select from gallery",
                modifier = Modifier
                    .padding(16.dp)
                    .size(35.dp)
            )
            Text(
                text = "Gallery", modifier = Modifier.padding(16.dp), fontSize = 16.sp
            )
        }
    }
}


object EditProfileRoute : NavigationRoute {
    override val route = "edit_profile"
    override val titleRes: Int = R.string.profile
}