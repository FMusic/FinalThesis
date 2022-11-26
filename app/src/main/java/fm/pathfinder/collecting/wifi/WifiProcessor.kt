package fm.pathfinder.collecting.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import android.widget.Toast
import fm.pathfinder.Constants
import fm.pathfinder.collecting.LocationCollector
import fm.pathfinder.fragments.MapsFragment
import fm.pathfinder.exceptions.WifiException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WifiProcessor(
    private val mContext: Context,
    private val locationCollector: LocationCollector
) : BroadcastReceiver() {
    private var rttSupport = false
    private var scanningOn = false
    private lateinit var mWifiManager: WifiManager

    private var mBackgroundExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor()

    init {
        try {
            initWifiRequests()
            initRtt()
        } catch (e: SecurityException) {
            Log.e(TAG, "SECURITY, NO WIFI WILL BE AVAILABLE")
            e.printStackTrace()

        }
    }

    private fun initRtt() {
        if (mContext.packageManager?.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT) != true) {
            Toast.makeText(mContext, "WIFI RTT IS NOT SUPPORTED ON THIS PHONE!", Toast.LENGTH_LONG)
                .show()
            Log.i("RTT", "WIFI RTT is NOT supported!")
            return
        }
        Log.i("RTT", "Wifi RTT should work")
        rttSupport = true

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
    }

    private fun initWifiRequests() {
        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (mContext.packageManager?.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT) == true) {
            intentFilter.addAction(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED)
        }
        mWifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mContext.registerReceiver(this, intentFilter)

        if (!mWifiManager.isWifiEnabled) {
            Toast.makeText(
                mContext, "Please turn wifi on, and enter map",
                Toast.LENGTH_SHORT
            ).show()
            throw WifiException("Wifi is not enabled!")
        } else {
            startScan()
            Log.i(TAG, "WIFI START SCAN")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {

            when (scanningOn) {
                true -> {
                    mBackgroundExecutor.schedule(
                        { startScan() },
                        Constants.SLEEP_TIME_MS,
                        TimeUnit.MILLISECONDS
                    )
                    // add wifi spots returns newly discovered wifi routers
                    // for which rtt should be initialized
                    initNewRangingRequest(
                        locationCollector.addWifiSpots(mWifiManager.scanResults)
                    )
                }
                false -> {
                    mBackgroundExecutor.schedule(
                        { startScan() },
                        Constants.SLEEP_NOSCAN_TIME_MS,
                        TimeUnit.MILLISECONDS
                    )
                    locationCollector.addWifiSpots(mWifiManager.scanResults)
                }
            }
        }
    }

    fun startScan() {
        mWifiManager.startScan()
        if (locationCollector.allWifiRouters.isNotEmpty()) {
            initNewRangingRequest(locationCollector.allWifiRouters)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initNewRangingRequest(apsToRange: List<ScanResult>) {
        if (!rttSupport)
            return
        val mgr =
            mContext.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager

        val request = if (apsToRange.size >= RangingRequest.getMaxPeers()) {
            RangingRequest.Builder()
                .addAccessPoints(apsToRange.subList(0, RangingRequest.getMaxPeers() - 1)).build()
        } else {
            RangingRequest.Builder().addAccessPoints(apsToRange).build()
        }
        mgr.startRanging(
            request,
            mContext.mainExecutor,
            WifiRangeCallback()
        )
    }

    companion object {
        private const val TAG: String = "WifiProcessor"
    }

}
