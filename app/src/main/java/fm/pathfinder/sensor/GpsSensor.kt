package fm.pathfinder.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import fm.pathfinder.model.Building
import fm.pathfinder.utils.Constants

@SuppressLint("MissingPermission")
class GpsSensor(
    context: Context,
    building: Building,
    val kFunction1: (LatLng) -> Unit
) : LocationListener {
    private var lastTimestamp: Long = 0
    private var locationManager: LocationManager

    init {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Constants.SLEEP_TIME_MS,
                Constants.MIN_DISTANCE_FEET,
                this
            )
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let {
                kFunction1(LatLng(it.latitude, it.longitude))
            }

        } else {
            Log.e(TAG, "Permission for GPS not granted")
            throw SecurityException("Permission for GPS not granted")
        }
    }

    override fun onLocationChanged(location: Location) {
        lastTimestamp = location.elapsedRealtimeNanos
        Log.i(TAG, "Location/GPS: $location")
        kFunction1(LatLng(location.latitude, location.longitude))
        locationManager.removeUpdates(this)
    }

    companion object {
        const val TAG = "GPSLocation"
    }


}