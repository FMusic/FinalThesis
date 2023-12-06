package fm.pathfinder.sensors

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import android.widget.Toast
import fm.pathfinder.utils.Constants
import fm.pathfinder.utils.WifiException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WifiSensor(
    private val mContext: Context,
    private val sensorCollector: SensorCollector
) : BroadcastReceiver() {
    private var lastTimestamp: Long = 0

    private var rttSupport = false
    private val rttScan = false
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
        if (!intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
            return
        }
        when (scanningOn) {
            true -> {
                lastTimestamp = System.currentTimeMillis()
                mBackgroundExecutor.schedule(
                    { startScan() },
                    Constants.SLEEP_TIME_MS,
                    TimeUnit.MILLISECONDS
                )
                initNewRangingRequest(mWifiManager.scanResults)
            }

            false -> {
                mBackgroundExecutor.schedule(
                    { startScan() },
                    Constants.SLEEP_NOSCAN_TIME_MS,
                    TimeUnit.MILLISECONDS
                )
            }
        }
    }

    fun startScan() = mWifiManager.startScan()

    @SuppressLint("MissingPermission")
    private fun initNewRangingRequest(apsToRange: List<ScanResult>) {
        if (!rttSupport && !rttScan)
            return
        val mgr = mContext.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager
        val request = if (apsToRange.size >= RangingRequest.getMaxPeers()) {
            RangingRequest.Builder()
                .addAccessPoints(apsToRange.subList(0, RangingRequest.getMaxPeers() - 1)).build()
        } else {
            RangingRequest.Builder().addAccessPoints(apsToRange).build()
        }
        mgr.startRanging(request, mContext.mainExecutor, WifiRangeCallback())
    }

    companion object {
        private const val TAG: String = "WIFI_SENSOR"
    }

    class WifiRangeCallback : RangingResultCallback() {
        override fun onRangingFailure(code: Int) {
            Log.e(TAG, "Code of ranging failure: $code")
        }

        override fun onRangingResults(results: MutableList<RangingResult>) {
            results.forEach {
                // filtering results:
                // https://developer.android.com/reference/android/net/wifi/rtt/RangingResult#constants_1
                if (it.status != 0) {
                    Log.i(TAG, "status of failing ${it.rssi} rtt result: ${it.status}")
                    return
                }
                Log.i(TAG, it.toString())
            }
        }


    }

}
