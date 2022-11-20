package fm.pathfinder.collecting

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import fm.pathfinder.Constants
import fm.pathfinder.fragments.MapsFragment

@SuppressLint("MissingPermission")
class GpsService(
    private val mapsFragment: MapsFragment,
    private val locationCollector: LocationCollector
) : LocationListener {

    init {
        val locationManager =
            mapsFragment.activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            Constants.SLEEP_TIME_MS,
            Constants.MIN_DISTANCE_FEET,
            this
        )
    }

    override fun onLocationChanged(location: Location) {
        Log.i(TAG, "Location/GPS: $location")
        mapsFragment.logIt("GPS: ${location.longitude}, ${location.latitude}")
        mapsFragment.changeLocation(LatLng(location.latitude, location.longitude))
        locationCollector.addLocation(location)
    }

    companion object  {
        const val TAG = "GPService"
    }
}