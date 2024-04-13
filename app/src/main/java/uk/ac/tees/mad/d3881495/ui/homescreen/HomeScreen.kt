package uk.ac.tees.mad.d3881495.ui.homescreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import uk.ac.tees.mad.d3881495.NavigationDestination
import uk.ac.tees.mad.d3881495.R

@Composable
fun HomeScreen(navController: NavHostController, onSignOut: () -> Unit) {
    Scaffold {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)) {
            Text(text = "Home screen")
            Button(onClick = onSignOut) {
                Text(text = "Sign out")
            }
        }
    }
}

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes: Int = R.string.home
}