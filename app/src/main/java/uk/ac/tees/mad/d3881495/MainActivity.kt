package uk.ac.tees.mad.d3881495

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.data.repository.GoogleAuthClient
import uk.ac.tees.mad.d3881495.ui.auth.LoginDestination
import uk.ac.tees.mad.d3881495.ui.auth.LoginScreen
import uk.ac.tees.mad.d3881495.ui.auth.RegisterDestination
import uk.ac.tees.mad.d3881495.ui.auth.RegisterScreen
import uk.ac.tees.mad.d3881495.ui.homescreen.HomeDestination
import uk.ac.tees.mad.d3881495.ui.homescreen.HomeScreen
import uk.ac.tees.mad.d3881495.ui.presentation.SplashScreen
import uk.ac.tees.mad.d3881495.ui.presentation.SplashScreenDestination
import uk.ac.tees.mad.d3881495.ui.theme.LocalSportsEquipmentSwapTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val googleAuthUiClient by lazy {
        GoogleAuthClient(
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    val firebase = FirebaseAuth.getInstance()

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
                    val currentUser = firebase.currentUser
                    val scope = rememberCoroutineScope()

                    val startDestination =
                        if ((currentUser != null) || (googleAuthUiClient.getSignedInUser() != null)) {
                            HomeDestination.route
                        } else {
                            LoginDestination.route
                        }

                    NavHost(
                        navController = navController,
                        startDestination = SplashScreenDestination.route
                    ) {
                        composable(SplashScreenDestination.route) {
                            SplashScreen(
                                onComplete = {
                                    scope.launch(Dispatchers.Main) {
                                        navController.popBackStack() // Clear any existing back stack
                                        navController.navigate(startDestination) // Navigate to the home screen
                                    }
                                }
                            )
                        }
                        composable(HomeDestination.route) {
                            HomeScreen(navController = navController, onSignOut = {
                                scope.launch {
                                    firebase.signOut()
                                    googleAuthUiClient.signOut()
                                    navController.navigate(LoginDestination.route)
                                }
                            })
                        }
                        composable(LoginDestination.route) {
                            LoginScreen(
                                loginSuccess = {
                                    Toast.makeText(
                                        applicationContext,
                                        "Login Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(HomeDestination.route)

                                },
                                onRegisterClick = {
                                    navController.navigate(RegisterDestination.route)
                                }
                            )
                        }
                        composable(RegisterDestination.route) {
                            RegisterScreen(
                                onLoginClick = { navController.navigate(LoginDestination.route) },
                                registerSuccess = {
                                    Toast.makeText(
                                        applicationContext,
                                        "Register Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(HomeDestination.route)

                                }
                            )
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