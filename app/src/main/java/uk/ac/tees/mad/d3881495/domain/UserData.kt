package uk.ac.tees.mad.d3881495.domain

data class UserData(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val image: ByteArray? = null
)

data class UserDataState(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val image: String? = null
)