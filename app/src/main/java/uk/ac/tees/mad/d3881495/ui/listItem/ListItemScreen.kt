package uk.ac.tees.mad.d3881495.ui.listItem

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.NavigationRoute
import uk.ac.tees.mad.d3881495.R
import uk.ac.tees.mad.d3881495.domain.Item
import uk.ac.tees.mad.d3881495.ui.PhotoOptions
import uk.ac.tees.mad.d3881495.utils.ApplicationViewModel
import uk.ac.tees.mad.d3881495.utils.LocationRepository
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddSportItemsScreen(
    viewModel: SportItemAddViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.addItemUiState.collectAsState()
    val context = LocalContext.current
    var showImagePicker by remember { mutableStateOf(false) }
    val addSportItemState =
        viewModel.addSportItemState.collectAsState(initial = null)
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    // Image selection and camera capture logic
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                val result = handleImageSelection(uri, context)
                viewModel.modifyViewState(uiState.value.copy(images = uiState.value.images + result))
            }
        }

    val requestCameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                val result = handleImageCapture(it)
                val images = uiState.value.images
                viewModel.modifyViewState(uiState.value.copy(images = images + result))
            }
        }

    val cameraPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    LaunchedEffect(addSportItemState.value?.data) {
        addSportItemState.value?.data?.let {
            Toast.makeText(context, "Sport item added successfully", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(addSportItemState.value?.error) {
        addSportItemState.value?.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "List item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Image picker bottom sheet
                if (showImagePicker) {
                    ModalBottomSheet(
                        onDismissRequest = { showImagePicker = false },
                        sheetState = bottomSheetState,
                        windowInsets = WindowInsets.ime,
                        content = {
                            PhotoOptions(
                                onGalleryClick = {
                                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                        if (!bottomSheetState.isVisible) {
                                            showImagePicker = false
                                        }
                                    }
                                    galleryLauncher.launch("image/*")
                                },
                                onCameraClick = {
                                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                        if (!bottomSheetState.isVisible) {
                                            showImagePicker = false
                                        }
                                    }
                                    if (!cameraPermissionState.status.isGranted) {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                    if (cameraPermissionState.status.isGranted) {
                                        requestCameraLauncher.launch(null)
                                    }
                                }
                            )
                        }
                    )
                }
                // UI for adding sport item details
                SportItemDetailsForm(
                    uiState = uiState.value,
                    onNameChange = { viewModel.modifyViewState(uiState.value.copy(name = it)) },
                    onDescriptionChange = { viewModel.modifyViewState(uiState.value.copy(description = it)) },
                    onPriceChange = {
                        viewModel.modifyViewState(uiState.value.copy(price = it))
                    },
                    onLocationChange = {
                        viewModel.modifyViewState(uiState.value.copy(listedItemLocation = it))
                    },
                    onAddressChange = {
                        viewModel.modifyViewState(uiState.value.copy(listedItemAddress = it))
                    },
                    onTypeSelected = { viewModel.modifyViewState(uiState.value.copy(type = it)) },
                    onConditionSelected = { viewModel.modifyViewState(uiState.value.copy(condition = it)) },
                    onImageAddClicked = {
                        showImagePicker = true
                    },
                    onSubmitClicked = {
                        if (validateSportItemFields(uiState.value, context)) {
                            viewModel.submitSportItem()
                        }
                    },
                    isLoading = addSportItemState.value?.isLoading == true,
                    onNavigateBack = {
                        viewModel.clearViewState()
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SportItemDetailsForm(
    uiState: Item,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onLocationChange: (GeoPoint) -> Unit,
    onAddressChange: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onConditionSelected: (String) -> Unit,
    onImageAddClicked: () -> Unit,
    onSubmitClicked: () -> Unit,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val applicationViewModel: ApplicationViewModel = hiltViewModel()
    val locationPermissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    val locationPermissionsState = rememberMultiplePermissionsState(
        locationPermissions
    )
    val activity = (context as ComponentActivity)
    val locationManager = LocationRepository(context, activity)
    val isGpsEnabled = locationManager.gpsStatus.collectAsState(initial = false)
    val location = Location("MyLocationProvider")

    val locationState =
        applicationViewModel.locationFlow.collectAsState(
            initial = location.apply {
                latitude = 51.509865
                longitude = -0.118092
            }
        )

    LaunchedEffect(locationState) {
        onLocationChange(GeoPoint(locationState.value.latitude, locationState.value.longitude))
    }
    var locationValue by remember {
        mutableStateOf("London, UK")
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text(text = "Title") },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text(text = "Description") },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        OutlinedTextField(
            value = uiState.price,
            onValueChange = onPriceChange,
            label = { Text(text = "Price (in Euro)") },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        OutlinedTextField(
            value = uiState.listedItemAddress,
            onValueChange = onAddressChange,
            label = { Text(text = "Location") },
            shape = MaterialTheme.shapes.small,
            trailingIcon = {
                IconButton(onClick = {
                    if (locationPermissionsState.allPermissionsGranted) {
                        if (!isGpsEnabled.value) {
                            locationManager.checkGpsSettings()
                        } else {
                            locationValue = locationManager.getAddressFromCoordinate(
                                latitude = locationState.value.latitude,
                                longitude = locationState.value.longitude
                            )
                            onAddressChange(locationValue)
                        }
                    } else {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        DropdownMenuComposable(
            list = listOf(
                "Ball Sports",
                "Fitness & Gym",
                "Water Sports",
                "Winter Sports",
                "Racquet Sports",
                "Outdoor & Adventure",
                "Track & Field",
                "Martial Arts & Boxing",
                "Equestrian",
                "Dance & Gymnastics",
                "Team Sports Gear",
                "Electronic Sports Equipment",
                "Miscellaneous"
            ),
            category = onTypeSelected,
            placeholder = "Type",
            displayText = {
                it
            },
            focusManager = focusManager
        )
        DropdownMenuComposable(
            list = listOf("New", "Used"),
            category = onConditionSelected,
            focusManager = focusManager,
            displayText = {
                it
            },
            placeholder = "Condition"
        )
        // And for "Condition":
//        DropdownMenuComposable(
//            list =,
//            onSelect = onConditionSelected,
//            label = "Condition"
//        )


        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.images) { item ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .crossfade(true)
                            .data(item)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    onClick = onImageAddClicked
                ) {
                    Image(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }

    Button(
        onClick = onSubmitClicked,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Text("Submit")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenuComposable(
    list: List<T>,
    category: (T) -> Unit,
    focusManager: FocusManager,
    displayText: (T) -> String,
    placeholder: String = "Select item"
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val options = list.map { displayText(it) to it }
        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember { mutableStateOf(placeholder) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = selectedOptionText,
                readOnly = true,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                })
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEachIndexed() { index, selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.first) },
                        onClick = {
                            selectedOptionText = selectionOption.first
                            category(options[index].second)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()

                    )
                }
            }
        }
    }
}

fun validateSportItemFields(uiState: Item, context: Context): Boolean {
    // Validation logic for sport item fields
    if (uiState.name.isBlank()) {
        Toast.makeText(context, "Please enter the item name", Toast.LENGTH_SHORT).show()
        return false
    }
    if (uiState.description.isBlank()) {
        Toast.makeText(context, "Please provide a description for the item", Toast.LENGTH_SHORT)
            .show()
        return false
    }
    if (uiState.type.isBlank()) {
        Toast.makeText(context, "Please select the item type", Toast.LENGTH_SHORT).show()
        return false
    }
    if (uiState.condition.isBlank()) {
        Toast.makeText(context, "Please select the item condition", Toast.LENGTH_SHORT).show()
        return false
    }
    if (uiState.images.isEmpty()) {
        Toast.makeText(context, "Please add at least one image of the item", Toast.LENGTH_SHORT)
            .show()
        return false
    }
    return true
}


fun handleImageSelection(uri: Uri, context: Context): ByteArray {
    val bitmap = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images
            .Media
            .getBitmap(context.contentResolver, uri)

    } else {
        val source = ImageDecoder
            .createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
    return convertBitmapToByteArray(bitmap)
}

fun handleImageCapture(bitmap: Bitmap): ByteArray {
    return convertBitmapToByteArray(bitmap)
}

fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

object ListItemRoute : NavigationRoute {
    override val route = "list_item"
    override val titleRes: Int = R.string.list_item
}