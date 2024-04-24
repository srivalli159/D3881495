package uk.ac.tees.mad.d3881495.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices.getSettingsClient
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.util.Locale

class LocationRepository(private val context: Context, private val activity: Activity) {

    private val settingsClient = getSettingsClient(context)
    private val locationRequest = LocationRequest()

    fun checkGpsSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            .setAlwaysShow(false)
            .setNeedBle(false)
        settingsClient.checkLocationSettings(builder.build())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result ?: return@addOnCompleteListener
                    val locationSettingsStates = response.locationSettingsStates
                    Log.e("yyy", locationSettingsStates.toString())
                    // TODO
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location status", "checkLocationSetting onFailure:" + e.message.toString())
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.d(
                            "Location status",
                            "Location settings are not satisfied. Attempting to upgrade " + "location settings "
                        )
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(activity, 0)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.d("Location status", "PendingIntent unable to execute request.")
                        }
                    }

                    else -> {
                    }
                }
            }
    }

    val gpsStatus = flow {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        while (currentCoroutineContext().isActive) {
            emit(
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            )
            delay(3000)
        }
    }

    fun getAddressFromCoordinate(
        latitude: Double,
        longitude: Double
    ): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        val city: String = addresses?.get(0)?.locality ?: "London"
        val country: String = addresses?.get(0)?.countryName ?: "UK"
        return "$city, $country"
    }
}