package fm.pathfinder.presenters

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import fm.pathfinder.fragments.MapsFragment
import fm.pathfinder.Constants
import fm.pathfinder.model.GpsSpot
import fm.pathfinder.model.toLatLng
import java.util.*

@SuppressLint("MissingPermission")
class MapPresenter(private val mapsFragment: MapsFragment) : LocationListener {
    private val linkedGps = LinkedList<GpsSpot>()
    init {
        try {
            val locationManager = mapsFragment.activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.SLEEP_TIME_MS, Constants.MIN_DISTANCE_FEET, this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        val gpsSpot = GpsSpot(location.altitude, location.latitude, location.longitude)
        mapsFragment.changeLocation(gpsSpot.toLatLng())
        linkedGps.add(gpsSpot)
    }
}