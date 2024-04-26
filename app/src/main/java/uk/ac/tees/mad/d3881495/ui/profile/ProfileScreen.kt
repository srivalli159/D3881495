package uk.ac.tees.mad.d3881495.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import uk.ac.tees.mad.d3881495.NavigationRoute
import uk.ac.tees.mad.d3881495.R
import uk.ac.tees.mad.d3881495.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    onFavorite: () -> Unit,
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {

    val userState by viewModel.getUserState.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        viewModel.getUserInformation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.profile)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(90.dp)
                        .border(1.dp, Color.Black, CircleShape)
                ) {
                    if (userState?.data?.image.isNullOrEmpty()) {
                        Image(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).crossfade(true)
                                .data(userState?.data?.image).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${userState?.data?.name}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )
                Text(text = "${userState?.data?.email}")
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onEditProfile() },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Edit profile", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { onFavorite() },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Favorite equipments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Sign out")
                }
            }
        }
    }
}

object ProfileRoute : NavigationRoute {
    override val route = "profile"
    override val titleRes: Int = R.string.profile
}