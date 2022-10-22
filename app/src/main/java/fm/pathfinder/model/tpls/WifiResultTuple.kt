package fm.pathfinder.model.tpls

import android.net.wifi.ScanResult

data class WifiResultTuple(val uniqueWifiResults: List<ScanResult>, val allWifiScanResults: ArrayList<ScanResult>)