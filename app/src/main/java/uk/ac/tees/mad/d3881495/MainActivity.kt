package uk.ac.tees.mad.d3881495

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.data.repository.GoogleAuthClient
import uk.ac.tees.mad.d3881495.ui.auth.LoginRoute
import uk.ac.tees.mad.d3881495.ui.auth.LoginScreen
import uk.ac.tees.mad.d3881495.ui.auth.RegisterRoute
import uk.ac.tees.mad.d3881495.ui.auth.RegisterScreen
import uk.ac.tees.mad.d3881495.ui.favorite.FavoriteRoute
import uk.ac.tees.mad.d3881495.ui.favorite.FavoriteScreen
import uk.ac.tees.mad.d3881495.ui.homescreen.HomeRoute
import uk.ac.tees.mad.d3881495.ui.homescreen.HomeScreen
import uk.ac.tees.mad.d3881495.ui.itemDetail.ItemDetailScreen
import uk.ac.tees.mad.d3881495.ui.itemDetail.ItemDetailsRoute
import uk.ac.tees.mad.d3881495.ui.listItem.AddSportItemsScreen
import uk.ac.tees.mad.d3881495.ui.listItem.ListItemRoute
import uk.ac.tees.mad.d3881495.ui.presentation.SplashScreen
import uk.ac.tees.mad.d3881495.ui.presentation.SplashScreenRoute
import uk.ac.tees.mad.d3881495.ui.profile.EditProfile
import uk.ac.tees.mad.d3881495.ui.profile.EditProfileRoute
import uk.ac.tees.mad.d3881495.ui.profile.ProfileRoute
import uk.ac.tees.mad.d3881495.ui.profile.ProfileScreen
import uk.ac.tees.mad.d3881495.ui.profile.ProfileViewModel
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
                    val profileViewModel: ProfileViewModel = hiltViewModel()
                    val getUserState by profileViewModel.getUserState.collectAsState(initial = null)

                    val startDestination =
                        if ((currentUser != null) || (googleAuthUiClient.getSignedInUser() != null)) {
                            HomeRoute.route
                        } else {
                            LoginRoute.route
                        }

                    NavHost(
                        navController = navController,
                        startDestination = SplashScreenRoute.route
                    ) {
                        composable(SplashScreenRoute.route) {
                            SplashScreen(
                                onComplete = {
                                    scope.launch(Dispatchers.Main) {
                                        navController.popBackStack() // Clear any existing back stack
                                        navController.navigate(startDestination) // Navigate to the home screen
                                    }
                                }
                            )
                        }
                        composable(HomeRoute.route) {
                            HomeScreen(
                                navController = navController,
                                onSportItemClick = {
                                    navController.navigate("${ItemDetailsRoute.route}/$it")
                                }
                            )
                        }
                        composable(
                            route = ItemDetailsRoute.routeWithArgs,
                            arguments = listOf(navArgument(ItemDetailsRoute.itemIdArg) {
                                type = NavType.StringType
                            })
                        ) {
                            ItemDetailScreen(
                                onBack = {
                                    navController.navigateUp()
                                }
                            )
                        }
                        composable(LoginRoute.route) {
                            LoginScreen(
                                loginSuccess = {
                                    Toast.makeText(
                                        applicationContext,
                                        "Login Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(HomeRoute.route)

                                },
                                onRegisterClick = {
                                    navController.navigate(RegisterRoute.route)
                                }
                            )
                        }
                        composable(RegisterRoute.route) {
                            RegisterScreen(
                                onLoginClick = { navController.navigate(LoginRoute.route) },
                                registerSuccess = {
                                    Toast.makeText(
                                        applicationContext,
                                        "Register Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(HomeRoute.route)

                                }
                            )
                        }
                        composable(ListItemRoute.route) {
                            AddSportItemsScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable(ProfileRoute.route) {
                            ProfileScreen(
                                onSignOut = {
                                    scope.launch {
                                        googleAuthUiClient.signOut()
                                        navController.navigate(LoginRoute.route)
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onEditProfile = { navController.navigate(EditProfileRoute.route) },
                                onFavorite = { navController.navigate(FavoriteRoute.route) },
                                viewModel = profileViewModel,
                                onBack = {
                                    navController.navigateUp()
                                }

                            )
                        }
                        composable(EditProfileRoute.route) {
                            EditProfile(
                                viewModel = profileViewModel, onComplete = {
                                    navController.navigateUp()
                                },
                                getUserState = getUserState
                            )
                        }
                        composable(FavoriteRoute.route) {
                            FavoriteScreen(
                                onItemClick = {
                                    navController.navigate(ItemDetailsRoute.route + "/" + it)
                                },
                                onBack = {
                                    navController.navigateUp()
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}

interface NavigationRoute {
    val route: String
    val titleRes: Int
}