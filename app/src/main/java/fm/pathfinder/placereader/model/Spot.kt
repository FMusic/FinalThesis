package fm.pathfinder.placereader.model

import fm.pathfinder.placereader.model.sensors.GpsSpot
import fm.pathfinder.placereader.model.sensors.MobileSpot
import fm.pathfinder.placereader.model.sensors.WifiAvailable
import java.time.LocalDate

class Spot {
    lateinit var gpsSpot: GpsSpot
    lateinit var mobileSpot: MobileSpot
    lateinit var wifiAvailable: List<WifiAvailable>

    var timestamp = LocalDate.now()

    fun isGpsInitialized() = ::gpsSpot.isInitialized
    fun isMobileInitialized() = ::mobileSpot.isInitialized
    fun isWifiInitialized() = ::wifiAvailable.isInitialized
}