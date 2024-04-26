package uk.ac.tees.mad.d3881495.data.repository

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.d3881495.domain.Item
import uk.ac.tees.mad.d3881495.domain.ItemResponse
import uk.ac.tees.mad.d3881495.domain.Resource
import uk.ac.tees.mad.d3881495.domain.UserModel
import java.util.Date
import java.util.UUID

interface FirestoreDatabaseRepository {
    fun uploadSportItemDetails(item: Item): Flow<Resource<String>>
    fun fetchAllSportItems(): Flow<Resource<List<ItemResponse>>>
    fun fetchItemDetailsById(uniqueItemId: String): Flow<Resource<ItemResponse>>
    fun fetchMyListedItems(): Flow<Resource<List<ItemResponse>>>
    fun getMultipleItemsWithKeys(itemIds: List<String>): Flow<Resource<List<ItemResponse>>>
}

class DatabaseRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : FirestoreDatabaseRepository {

    override fun uploadSportItemDetails(item: Item): Flow<Resource<String>> = callbackFlow {
        val uniqueId = UUID.randomUUID()
        val storageReference = firebaseStorage.reference.child("images/$uniqueId")

        trySend(Resource.Loading())

        val uploadImageTasks = item.images.map { image ->
            storageReference.putBytes(image)
        }

        Tasks.whenAllSuccess<UploadTask.TaskSnapshot>(uploadImageTasks)
            .addOnSuccessListener { snapshots ->
                val urlsTasks = snapshots.map { it.storage.downloadUrl }
                Tasks.whenAllSuccess<Uri>(urlsTasks).addOnSuccessListener { uris ->
                    val itemData = hashMapOf(
                        "name" to item.name,
                        "description" to item.description,
                        "listedDate" to item.listedDate,
                        "images" to uris.map(Uri::toString),
                        "type" to item.type,
                        "condition" to item.condition,
                        "listedByKey" to item.listedByKey,
                        "price" to item.price,
                        "status" to "Listed",
                        "address" to item.listedItemAddress,
                        "location" to item.listedItemLocation
                    )

                    firestore.collection("sportItems")
                        .add(itemData)
                        .addOnSuccessListener { docRef ->
                            Log.d("Upload Success", "Item uploaded with ID: ${docRef.id}")
                            trySend(Resource.Success(docRef.id))

                            val currentUser = Firebase.auth.currentUser?.uid ?: ""
                            val userRef = firestore.collection("users").document(currentUser)
                            firestore.runTransaction { transaction ->
                                val snapshot = transaction.get(userRef)
                                val userItems = snapshot.get("listedItems") as? MutableList<String>
                                    ?: mutableListOf()
                                userItems.add(docRef.id)
                                transaction.update(userRef, "listedItems", userItems)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Upload Failure", "Failed to upload item", exception)
                            trySend(Resource.Error("Failed to upload item"))
                        }
                }
            }
        awaitClose { close() }
    }

    override fun fetchAllSportItems(): Flow<Resource<List<ItemResponse>>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val querySnapshot = firestore.collection("sportItems").get().await()
            val itemList = querySnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                ItemResponse(
                    id = doc.id,
                    name = data["name"] as String,
                    description = data["description"] as String,
                    images = data["images"] as List<String>,
                    condition = data["condition"] as String,
                    type = data["type"] as String,
                    listedDate = (data["listedDate"] as Timestamp).toDate(),
                    status = data["status"] as String,
                    price = data["price"] as String,
                    listedItemAddress = data["address"] as String,
                    listedItemLocation = data["location"] as GeoPoint
                )
            }
            trySend(Resource.Success(itemList))
        } catch (e: Exception) {
            Log.w("Fetch Failure", "Error retrieving items", e)
            trySend(Resource.Error("Error retrieving items"))
        }
        awaitClose { close() }
    }

    override fun fetchItemDetailsById(uniqueItemId: String): Flow<Resource<ItemResponse>> =
        callbackFlow {
            trySend(Resource.Loading()) // Indicate loading state
            try {
                println(uniqueItemId)

                val itemDocumentRef = firestore.collection("sportItems").document(uniqueItemId)
                val documentSnapshot = itemDocumentRef.get().await()

                if (!documentSnapshot.exists()) {
                    println("Document not found in the collection")
                    trySend(Resource.Error("Item not found"))
                    awaitClose { close() }
                    return@callbackFlow
                }

                val itemData = documentSnapshot.data ?: kotlin.run {
                    println("Document is empty")
                    trySend(Resource.Error("No data available for the item"))
                    awaitClose { close() }
                    return@callbackFlow
                }

                val ownerUserId = itemData["listedByKey"] as? String ?: ""
                val ownerUserSnapshot =
                    firestore.collection("users").document(ownerUserId).get().await()
                val listedBy = if (ownerUserSnapshot.exists()) {
                    val userData = ownerUserSnapshot.data
                    UserModel(
                        name = userData?.get("name") as? String ?: "",
                        email = userData?.get("email") as? String ?: "",
                        phone = userData?.get("phone") as? String ?: "",
                        profileImage = userData?.get("images") as? String ?: "",
                        location = userData?.get("location") as? String ?: ""
                    )
                } else null

                val itemResponse = ItemResponse(
                    id = uniqueItemId,
                    name = itemData["name"] as String? ?: "Unknown",
                    description = itemData["description"] as String? ?: "Unknown",
                    type = itemData["type"] as String? ?: "Unknown",
                    condition = itemData["condition"] as String? ?: "Unknown",
                    price = itemData["price"] as String? ?: "Unknown",
                    listedBy = listedBy,
                    listedItemLocation = itemData["location"] as GeoPoint? ?: GeoPoint(0.0, 0.0),
                    listedItemAddress = itemData["address"] as String? ?: "Unknown",
                    listedDate = (itemData["listedDate"] as Timestamp?)?.toDate() ?: Date(),
                    images = itemData["images"] as List<String>? ?: emptyList(),
                    status = itemData["status"] as String? ?: "Unknown"
                )

                trySend(Resource.Success(itemResponse))
            } catch (exception: Exception) {
                Log.e("Fetch Error", "Failed to fetch item details", exception)
                trySend(Resource.Error("Failed to fetch item details"))
            }

            awaitClose { close() }
        }


    override fun fetchMyListedItems(): Flow<Resource<List<ItemResponse>>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val querySnapshot = firestore.collection("sportItems")
                .whereEqualTo("listedByKey", Firebase.auth.currentUser?.uid).get().await()
            val itemList = querySnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                ItemResponse(
                    id = doc.id,
                    name = data["name"] as String,
                    description = data["description"] as String,
                    images = data["images"] as List<String>,
                    condition = data["condition"] as String,
                    type = data["type"] as String,
                    listedDate = (data["listedDate"] as Timestamp).toDate(),
                    status = data["status"] as String,
                    price = data["price"] as String,
                    listedItemAddress = data["address"] as String,
                    listedItemLocation = data["location"] as GeoPoint
                )
            }
            trySend(Resource.Success(itemList))
        } catch (e: Exception) {
            Log.w("Fetch Failure", "Error retrieving items", e)
            trySend(Resource.Error("Error retrieving items"))
        }
        awaitClose { close() }
    }

    override fun getMultipleItemsWithKeys(itemIds: List<String>): Flow<Resource<List<ItemResponse>>> =
        callbackFlow {

            trySend(Resource.Loading())
            try {
                val responseList = mutableListOf<ItemResponse>()
                for (itemId in itemIds) {
                    val document =
                        FirebaseFirestore.getInstance().collection("sportItems").document(itemId)
                            .get().await()
                    if (document.exists()) {
                        val data = document.data
                        if (data != null) {
                            responseList.add(
                                ItemResponse(
                                    id = document.id,
                                    name = data["name"] as String,
                                    description = data["description"] as String,
                                    images = data["images"] as List<String>,
                                    condition = data["condition"] as String,
                                    type = data["type"] as String,
                                    listedDate = (data["listedDate"] as Timestamp).toDate(),
                                    status = data["status"] as String,
                                    price = data["price"] as String,
                                    listedItemAddress = data["address"] as String,
                                    listedItemLocation = data["location"] as GeoPoint
                                )
                            )
                        }
                    }
                }
                trySend(Resource.Success(responseList))
            } catch (ex: Exception) {
                ex.printStackTrace()
                trySend(Resource.Error("Error fetching Items"))
            }
            awaitClose { close() }
        }

}