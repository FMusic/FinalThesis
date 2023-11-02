package fm.pathfinder.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import fm.pathfinder.utils.Constants

@SuppressLint("MissingPermission")
class GpsSensor(
    context: Context,
    private val logFunc: (String) -> Unit,
    private val locationScanner: LocationScanner
) : LocationListener {

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
        Log.i(TAG, "Location/GPS: $location")
        logFunc("GPS: ${location.longitude}, ${location.latitude}")
        locationScanner.addLocation(location)
    }

    companion object  {
        const val TAG = "GPService"
    }
}