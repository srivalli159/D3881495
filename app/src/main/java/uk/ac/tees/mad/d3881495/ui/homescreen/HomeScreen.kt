package uk.ac.tees.mad.d3881495.ui.homescreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import uk.ac.tees.mad.d3881495.NavigationRoute
import uk.ac.tees.mad.d3881495.R
import uk.ac.tees.mad.d3881495.domain.ItemResponse
import uk.ac.tees.mad.d3881495.ui.listItem.ListItemRoute
import uk.ac.tees.mad.d3881495.ui.profile.ProfileRoute
import uk.ac.tees.mad.d3881495.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onSportItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val itemListState by viewModel.itemsList.collectAsState(initial = null)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var searchFieldValue by remember {
        mutableStateOf("")
    }
    var filteredItems by remember {
        mutableStateOf(emptyList<ItemResponse>())
    }

    LaunchedEffect(Unit) {
        viewModel.getSportsItemList()
    }
    LaunchedEffect(key1 = itemListState) {
        itemListState?.isError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        itemListState?.isSuccess?.let {
            filteredItems = it
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ListItemRoute.route) },
                containerColor = PrimaryBlue
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add item",
                    tint = Color.White
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(ProfileRoute.route)
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(0.1f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SearchField(
                    text = searchFieldValue,
                    focusManager = focusManager,
                    onChange = { value ->
                        searchFieldValue = value
                        itemListState?.isSuccess?.let {
                            filteredItems = it.filter { item ->
                                item.name.lowercase().contains(searchFieldValue.lowercase())
                                        || item.type.lowercase()
                                    .contains(searchFieldValue.lowercase())
                                        || item.description.lowercase()
                                    .contains(searchFieldValue.lowercase())
                            }
                        }
                    })
                Row() {
                    Text(text = "Listed items", fontSize = 22.sp, fontWeight = FontWeight.Medium)
                }
            }
            if (itemListState?.isLoading == true) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (itemListState?.isSuccess.isNullOrEmpty()) {
                        item {
                            Text(text = "No items present")
                        }
                    } else {
                        items(itemListState?.isSuccess!!) {
                            Card(
                                elevation = CardDefaults.elevatedCardElevation(8.dp),
                                colors = CardDefaults.cardColors(Color.White)
                            ) {
                                SportItem(
                                    item = it,
                                    onClick = { onSportItemClick(it.id) },
                                    onLike = {
                                        viewModel.addItemToFavorite(it.id)
                                        Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SearchField(
    text: String,
    focusManager: FocusManager,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = ""
            )
        },
        placeholder = {
            Text(text = "Search..")
        },
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        )
    )
}

@Composable
fun SportItem(
    item: ItemResponse,
    onClick: () -> Unit,
    onLike: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).crossfade(true)
                    .data(item.images[0])
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onLike,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))

                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null)
                }
            }
        }
        HorizontalDivider()
        Column(Modifier.padding(8.dp)) {
            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = item.type, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

object HomeRoute : NavigationRoute {
    override val route = "home"
    override val titleRes: Int = R.string.home
}