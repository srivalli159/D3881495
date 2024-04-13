package uk.ac.tees.mad.d3881495.data.repository

import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.d3881495.domain.Resource

interface AuthRepository {
    fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
    fun registerUser(email: String, password: String, username: String): Flow<Resource<AuthResult>>
    suspend fun saveUser(email: String?, username: String?, userId: String?)
}