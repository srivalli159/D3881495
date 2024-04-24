package uk.ac.tees.mad.d3881495.domain

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Item(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val condition: String = "",
    val price: String = "",
    val listedByKey: String = "",
    val listedItemLocation: GeoPoint = GeoPoint(0.0, 0.0),
    val listedItemAddress: String = "",
    val listedDate: Date = Date(),
    val images: List<ByteArray> = emptyList()
)

data class ItemResponse(
    val id: String ,
    val name: String ,
    val description: String ,
    val type: String ,
    val condition: String ,
    val price: String ,
    val listedBy:UserModel? = null,
    val listedItemLocation: GeoPoint = GeoPoint(0.0, 0.0),
    val listedItemAddress: String = "",
    val listedDate: Date = Date(),
    val images: List<String> = emptyList(),
    val status: String
)

data class UserModel(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImage: String = "",
    val listedItems: List<String> = emptyList(),
    val location: String = ""
)