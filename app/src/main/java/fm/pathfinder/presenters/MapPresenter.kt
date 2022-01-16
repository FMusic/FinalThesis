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
import fm.pathfinder.model.WifiResult
import fm.pathfinder.model.toLatLng
import fm.pathfinder.processor.LocationScanner
import java.util.*

@SuppressLint("MissingPermission")
class MapPresenter(private val mapsFragment: MapsFragment) : LocationListener {
    private val locationScanner = LocationScanner()
    private var scanningOn = false
    private lateinit var wifiManager: WifiManager

    private lateinit var timer: Timer

    init {
        try {
            initLocationRequests()
            initWifiRequests()
        } catch (e: SecurityException) {
            Log.e(TAG, "SECURITY, NO WIFI WILL BE AVAILABLE")
            e.printStackTrace()
        }
    }

    // region   gps location
    private fun initLocationRequests() {
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
        Log.i(TAG, location.toString())
        mapsFragment.changeLocation(location.toLatLng())
        if (scanningOn)
            locationScanner.addLocation(
                location.latitude, location.longitude,
                location.altitude, location.accuracy, location.time
            )
    }

    // endregion
    // region    wifi
    private fun initWifiRequests() {
        wifiManager =
            mapsFragment.activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if(mapsFragment.context != null){
            mapsFragment.requireContext().registerReceiver(wifiScanReceiver, intentFilter)
        }else{
            Log.e(TAG,"NO CONTEXT IN WIFI!")
            mapsFragment.requireContext().registerReceiver(wifiScanReceiver, intentFilter)
        }

        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(
                mapsFragment.context,
                "Please turn wifi on, and enter map",
                Toast.LENGTH_SHORT
            )
                .show()
            mapsFragment.activity?.onBackPressed()
        } else {
//                setting up timer which cancels all events when scanning is turning off
//            timer = Timer()
//            val timerTask = (object : TimerTask() {
//                override fun run() {
//                    Log.i(TAG, "Wifi Start Scan Event")
//                    wifiManager.startScan()
//                }
//            })
//            timer.schedule(timerTask, 0, 10000)
            wifiManager.startScan()
            Log.i(TAG, "WIFI START SCAN EVENT")
        }
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val succ = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED , false)
            if (succ) {
                val scanResults = wifiManager.scanResults.map{x->
                    WifiResult(x.BSSID, x.SSID, x.capabilities, x.frequency, x.level, x.timestamp)
                }.toList()
                mapsFragment.logWifi()
                Log.d(TAG, scanResults.toString())
            }
        }
    }
    // endregion

    fun startScan() {
        scanningOn = true
    }

    fun stopScan() {
        scanningOn = false
//        timer.cancel()
    }

    fun newRoom(label: String) {
        Toast.makeText(mapsFragment.context, "New Room Add $label", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG: String = "MapPresenter"
    }
}