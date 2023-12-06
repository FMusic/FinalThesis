package fm.pathfinder.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import fm.pathfinder.model.GpsLocation
import fm.pathfinder.utils.Building
import fm.pathfinder.utils.Constants
import java.time.LocalDateTime

@SuppressLint("MissingPermission")
class GpsSensor(
    context: Context,
    building: Building
) : LocationListener {
    private var lastTimestamp: Long = 0

    init {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            Constants.SLEEP_TIME_MS,
            Constants.MIN_DISTANCE_FEET,
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        lastTimestamp = location.elapsedRealtimeNanos
        Log.i(TAG, "Location/GPS: $location")
        val gpsLocation = GpsLocation(
            location.latitude,
            location.longitude,
            location.altitude,
            location.accuracy,
            location.time,
            LocalDateTime.now()
        )
    }

    companion object  {
        const val TAG = "GPSLocation"
    }



}