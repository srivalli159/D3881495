package uk.ac.tees.mad.d3881495.domain

data class LoginState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)

data class LoginStatus(
    val isLoading: Boolean = false,
    val isSuccess: String? = "",
    val isError: String? = ""
)


data class SignInResult(
    val data: UserData? = null,
    val isSuccessful: Boolean = false,
    val errorMessage: String? = null
)