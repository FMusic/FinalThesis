package fm.pathfinder.presenters

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.util.Log
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
    private lateinit var wifiManager: WifiManager

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val succ = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (succ) {
                getResultsForWifiScan()
            }
        }
    }

    init {
        try {
            val locationManager =
                mapsFragment.activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Constants.SLEEP_TIME_MS,
                Constants.MIN_DISTANCE_FEET,
                this
            )

            wifiManager =
                mapsFragment.activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            mapsFragment.context?.registerReceiver(wifiScanReceiver, intentFilter)

            if (!wifiManager.isWifiEnabled) {
                Toast.makeText(mapsFragment.context, "Please turn wifi on", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.i(TAG, location.toString())
        mapsFragment.changeLocation(location.toLatLng())
        if (scanningOn)
            locationScanner.addLocation(
                location.latitude, location.longitude,
                location.altitude, location.accuracy, location.time
            )
    }



    private fun getResultsForWifiScan() {
        val scanResults = wifiManager.scanResults
        Log.d(TAG, scanResults.toString())
    }

    fun startScan() {
        scanningOn = true
    }

    fun stopScan() {
        scanningOn = false
    }

    fun newRoom(label: String) {
        Toast.makeText(mapsFragment.context, "New Room Add $label", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG: String = "MapPresenter"
    }
}