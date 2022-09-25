package fm.pathfinder.presenters

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import fm.pathfinder.Constants
import fm.pathfinder.MainActivity
import fm.pathfinder.fragments.MapsFragment
import fm.pathfinder.sensors.LocationScanner
import java.util.*

@SuppressLint("MissingPermission")
class MapPresenter(
    private val mapsFragment: MapsFragment,
    private val mainActivity: MainActivity
) {
    private var scanningOn = false
    private lateinit var wifiManager: WifiManager

    private lateinit var timer: Timer

    private var locationScanner = LocationScanner(mapsFragment, this)
    private var rttSupport = false

    init {

        try {
            initLocationRequests()
            initWifiRequests()
//            initMagneticRequests()
            initWifiRttRequests()
        } catch (e: SecurityException) {
            Log.e(TAG, "SECURITY, NO WIFI WILL BE AVAILABLE")
            e.printStackTrace()
        }
    }

    private fun initWifiRttRequests() {
        if (!mainActivity.applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
        ) {
            Toast.makeText(
                mapsFragment.context,
                "WIFI RTT IS NOT SUPPORTED ON THIS PHONE!",
                Toast.LENGTH_LONG
            ).show()
            Log.i("RTT", "WIFI RTT is NOT supported!")
            return
        }
        Log.i("RTT", "Wifi RTT should work")
//        TODO: register a broadcast receiver to receive state changed
/*
        val filter = IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED)
        val myReceiver = object: BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                if (wifiRttManager.isAvailable) {
                    …
                } else {
                    …
                }
            }
        }
        context.registerReceiver(myReceiver, filter)
*/
        rttSupport = true
    }

    public fun initNewRangingRequest(apsToRange: List<ScanResult>) {
        if (!rttSupport)
            throw Exception("rtt support does not exist")
        val mgr =
            mapsFragment.context?.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager
        val request: RangingRequest = RangingRequest.Builder().addAccessPoints(apsToRange).build()
        mgr.startRanging(
            request,
            mapsFragment.requireContext().mainExecutor,
            object : RangingResultCallback() {

                override fun onRangingResults(results: List<RangingResult>) {
                    results.forEach {
                        Log.i("rtt", it.toString())
                    }
                }

                override fun onRangingFailure(code: Int) {
                    Log.e("rtt", "code of ranging failure: $code")
                }
            })
    }

    private fun initMagneticRequests() {
        TODO("Not yet implemented")
    }

    // region   gps location
    private fun initLocationRequests() {
        val locationManager =
            mapsFragment.activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            Constants.SLEEP_TIME_MS,
            Constants.MIN_DISTANCE_FEET,
            locationScanner
        )
    }

    // endregion
    // region    wifi
    private fun initWifiRequests() {
//        check for RTT support
        val ctx = mapsFragment.activity?.applicationContext
        if (ctx?.packageManager?.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT) == true) {
            val filter = IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED)
            ctx.registerReceiver(locationScanner, filter)
        }
        wifiManager = ctx?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (mapsFragment.context != null) {
            mapsFragment.requireContext().registerReceiver(locationScanner, intentFilter)
        } else {
            Log.e(TAG, "NO CONTEXT IN WIFI!")
            mapsFragment.requireContext().registerReceiver(locationScanner, intentFilter)
        }

        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(
                mapsFragment.context, "Please turn wifi on, and enter map",
                Toast.LENGTH_SHORT
            ).show()
            mapsFragment.activity?.onBackPressed()
        } else {
            wifiManager.startScan()
            Log.i(TAG, "WIFI START SCAN EVENT")
        }
    }
    // endregion

    fun startScan() {
        scanningOn = true
    }

    fun stopScan() {
        scanningOn = false
        val buildingData = locationScanner.extractData()
        val jsonBuilding = Gson().toJson(buildingData)
        mainActivity.createFile(jsonBuilding)
        Toast.makeText(mapsFragment.context, "Saving building data", Toast.LENGTH_SHORT).show()
    }

    fun newRoom(roomName: String) {
        locationScanner.enterNewRoom(roomName)
        Toast.makeText(mapsFragment.context, "New Room enter $roomName", Toast.LENGTH_SHORT).show()
    }

    fun exitRoom() {
        val roomName = locationScanner.exitRoom()
        Toast.makeText(mapsFragment.context, "Room exited $roomName", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG: String = "MapPresenter"
    }
}