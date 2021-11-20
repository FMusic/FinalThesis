package fm.pathfinder.placereader.bl.scanners

import android.content.Context
import android.net.wifi.ScanResult
import fm.pathfinder.placereader.model.sensors.WifiAvailable
import android.net.wifi.WifiManager as WifiManager

class WifiScanner(
    ctx: Context
) : Scanner() {
    private var wifiManager: WifiManager

    init{
        shouldScan = true
        wifiManager = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun scan(){
        super.scan()
        while(shouldScan){
            process(wifiManager.scanResults)
        }
    }

    private fun process(scanResults: List<ScanResult>) {
        val wifiSpots = ArrayList<WifiAvailable>()
        scanResults.forEach { wifiSpots.add(WifiAvailable(it.SSID, it.level)) }
        listeners.forEach{it.onNewWifiDetected(wifiSpots)}
    }
}