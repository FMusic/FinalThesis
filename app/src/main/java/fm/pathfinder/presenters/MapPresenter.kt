package fm.pathfinder.presenters

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.widget.Toast
import fm.pathfinder.fragments.MapsFragment
import fm.pathfinder.Constants
import fm.pathfinder.model.GpsSpot
import fm.pathfinder.model.toLatLng
import fm.pathfinder.processor.LocationScanner
import java.util.*

@SuppressLint("MissingPermission")
class MapPresenter(private val mapsFragment: MapsFragment) : LocationListener {
    private val locationScanner = LocationScanner()
    private var scanningOn = false

    init {
        try {
            val locationManager = mapsFragment.activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.SLEEP_TIME_MS, Constants.MIN_DISTANCE_FEET, this)
//            val wifiManager = mapsFragment.activity?.getSystemService(Context.WIFI_SERVICE) as WifiManager

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        mapsFragment.changeLocation(location.toLatLng())
        if (scanningOn)
            locationScanner.addLocation(location.latitude, location.longitude,
                location.altitude, location.accuracy, location.time)
    }

    fun startScan() {
        scanningOn = true
    }

    fun stopScan() {
        scanningOn = false
    }

    fun newRoom(label: String) {
        Toast.makeText(mapsFragment.context, "New Room Add $label",Toast.LENGTH_LONG).show()
    }
}