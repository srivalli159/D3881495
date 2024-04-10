package uk.ac.tees.mad.d3881495

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.d3881495.ui.homescreen.HomeDestination
import uk.ac.tees.mad.d3881495.ui.homescreen.HomeScreen
import uk.ac.tees.mad.d3881495.ui.presentation.SplashScreen
import uk.ac.tees.mad.d3881495.ui.presentation.SplashScreenDestination
import uk.ac.tees.mad.d3881495.ui.theme.LocalSportsEquipmentSwapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalSportsEquipmentSwapTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable(SplashScreenDestination.route) {
                            SplashScreen(navController = (navController))
                        }
                        composable(HomeDestination.route) {
                            HomeScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

interface NavigationDestination {
    val route: String
    val titleRes: Int
}