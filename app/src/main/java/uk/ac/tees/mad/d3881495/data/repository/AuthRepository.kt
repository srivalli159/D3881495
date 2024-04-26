package uk.ac.tees.mad.d3881495.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.d3881495.domain.Resource
import uk.ac.tees.mad.d3881495.domain.UserData
import uk.ac.tees.mad.d3881495.domain.UserDataState
import java.util.UUID
import javax.inject.Inject

interface AuthRepository {
    fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
    fun registerUser(email: String, password: String, username: String): Flow<Resource<AuthResult>>
    suspend fun saveUser(email: String?, username: String?, userId: String?)
    suspend fun updateCurrentUser(user: UserData): Flow<Resource<String>>
    fun getCurrentUser(): Flow<Resource<UserDataState>>
}


class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : AuthRepository {

    override fun loginUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun registerUser(
        email: String,
        password: String,
        username: String
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            // Add user to Firestore with name
            val userId = authResult.user?.uid
            saveUser(userId = userId, email = email, username = username)
            emit(Resource.Success(authResult))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override suspend fun saveUser(email: String?, username: String?, userId: String?) {
        if (userId != null) {
            val userMap = hashMapOf(
                "email" to email,
                "name" to username
                // Add other user data if needed
            )
            firebaseFirestore.collection("users").document(userId).set(userMap).await()
        }
    }


    override suspend fun updateCurrentUser(user: UserData): Flow<Resource<String>> =
        callbackFlow {
            trySend(Resource.Loading())
            val currentUserUid = firebaseAuth.currentUser?.uid

            val storage = Firebase.storage.reference
            val imageRef = storage.child("user/${UUID.randomUUID()}")
            val uploadTask = user.image?.let {
                imageRef.putBytes(it)
            }

            uploadTask?.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val map = HashMap<String, Any>().apply {
                        put("name", user.name)
                        put("address", user.address)
                        put("image", uri.toString())
                    }
                    if (currentUserUid != null) {
                        firebaseFirestore.collection("users")
                            .document(currentUserUid)
                            .update(map)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    trySend(Resource.Success("Updated Successfully.."))
                                }
                            }
                            .addOnFailureListener { e ->
                                trySend(Resource.Error(message = e.message))
                            }
                    } else {
                        trySend(Resource.Error(message = "User not logged in"))
                    }
                }
            }?.addOnFailureListener { e ->
                trySend(Resource.Error(message = e.message))
            }

            awaitClose { close() }
        }


    override fun getCurrentUser(): Flow<Resource<UserDataState>> = callbackFlow {
        trySend(Resource.Loading())

        val userId = Firebase.auth.currentUser?.uid!!

        firebaseFirestore.collection("users").document(userId).get()
            .addOnSuccessListener { mySnapshot ->
                if (mySnapshot.exists()) {
                    val data = mySnapshot.data
                    if (data != null) {
                        val userResponse = UserDataState(
                            userId = userId,
                            name = data["name"] as String? ?: "",
                            email = data["email"] as String? ?: "",
                            address = data["address"] as String? ?: "",
                            image = data["image"] as String? ?: ""
                        )

                        trySend(Resource.Success(userResponse))
                    } else {
                        println("No data found in Database")
                    }
                } else {
                    println("No data found in Database")
                }
            }.addOnFailureListener { e ->
                e.printStackTrace()
                trySend(Resource.Error(message = e.toString()))
            }
        awaitClose {
            close()
        }
    }

}