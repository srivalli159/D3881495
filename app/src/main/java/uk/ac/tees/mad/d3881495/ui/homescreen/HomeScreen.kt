package uk.ac.tees.mad.d3881495.ui.homescreen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import uk.ac.tees.mad.d3881495.NavigationDestination
import uk.ac.tees.mad.d3881495.R

@Composable
fun HomeScreen(navController: NavHostController) {
    Text(text = "Home screen")
}

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes: Int = R.string.app_name
}