package uk.ac.tees.mad.d3881495.ui.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3881495.NavigationRoute
import uk.ac.tees.mad.d3881495.R
import uk.ac.tees.mad.d3881495.data.repository.GoogleAuthClient
import uk.ac.tees.mad.d3881495.ui.theme.PrimaryBlue

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    loginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {

    val signInStatus = viewModel.signInState.collectAsState(initial = null)
    val signInState = viewModel.state.collectAsState().value
    val loginUiState = viewModel.loginUiState.collectAsState().value
    val focusManager = LocalFocusManager.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleAuthUiClient by lazy {
        GoogleAuthClient(
            oneTapClient = Identity.getSignInClient(context)
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                    viewModel.onSignInWithGoogleResult(signInResult)
                }
            }
        }
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(PrimaryBlue)

    ) {
        Column {
            Row(
                Modifier
                    .height(170.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome Back!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 120.dp))
                    .background(Color.White)
                    .padding(30.dp)
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Please login with your personal information",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
                Spacer(modifier = Modifier.height(60.dp))

                OutlinedTextField(
                    value = loginUiState.email,
                    onValueChange = {
                        viewModel.updateLoginState(loginUiState.copy(email = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = "Email")
                    },
                    maxLines = 1,
                    visualTransformation = VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Email, contentDescription = "")
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = loginUiState.password,
                    onValueChange = {
                        viewModel.updateLoginState(loginUiState.copy(password = it))
                    },

                    modifier = Modifier.fillMaxWidth(),

                    maxLines = 1,
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Outlined.Visibility
                        else Icons.Outlined.VisibilityOff

                        val description =
                            if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                description,
                            )
                        }
                    },
                    placeholder = {
                        Text(text = "Password")
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = "")
                    }
                )
                Spacer(modifier = Modifier.height(40.dp))


                Button(
                    onClick = { viewModel.loginUser(loginUiState.email, loginUiState.password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    if (signInStatus.value?.isLoading == true) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.background)
                    } else {
                        Text(text = "Login", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "or Login with")
                Spacer(modifier = Modifier.height(24.dp))
                IconButton(
                    onClick = {
                        scope.launch {
                            val signInIntentSender = googleAuthUiClient.signIn()
                            launcher.launch(
                                IntentSenderRequest
                                    .Builder(
                                        signInIntentSender ?: return@launch
                                    )
                                    .build()
                            )
                        }
                    },
                    modifier = Modifier
                        .border(BorderStroke(1.dp, Color.Gray), CircleShape)
                        .padding(5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Sign with google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Don't have an account? ")
                    Text(
                        text = "Sign Up",
                        modifier = Modifier.clickable {
                            onRegisterClick()
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        LaunchedEffect(key1 = signInStatus.value?.isSuccess) {
            scope.launch {
                if (signInStatus.value?.isSuccess?.isNotEmpty() == true) {
                    focusManager.clearFocus()
                    val success = signInStatus.value?.isSuccess
                    Toast.makeText(context, "$success", Toast.LENGTH_LONG).show()
                    loginSuccess()
                }
            }
        }

        LaunchedEffect(key1 = signInStatus.value?.isError) {
            scope.launch {
                if (signInStatus.value?.isError?.isNotEmpty() == true) {
                    val error = signInStatus.value?.isError
                    Toast.makeText(context, "$error", Toast.LENGTH_LONG).show()
                }
            }
        }
        LaunchedEffect(key1 = signInState.isSignInSuccessful) {
            if (signInState.isSignInSuccessful) {
                Toast.makeText(
                    context,
                    "Sign in successful",
                    Toast.LENGTH_LONG
                ).show()
                val user = googleAuthUiClient.getSignedInUser()
                if (user != null) {
                    viewModel.saveUserInFirestore(user)
                }
                viewModel.resetState()
                loginSuccess()
            }
        }
        LaunchedEffect(key1 = signInState.signInError) {
            scope.launch {
                if (signInState.signInError?.isNotEmpty() == true) {
                    val error = signInState.signInError
                    Toast.makeText(context, "$error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

object LoginRoute : NavigationRoute {
    override val route = "login"
    override val titleRes: Int = R.string.login
}