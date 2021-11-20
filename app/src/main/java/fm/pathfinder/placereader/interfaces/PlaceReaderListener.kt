package fm.pathfinder.placereader.interfaces

import fm.pathfinder.placereader.model.sensors.GpsSpot
import fm.pathfinder.placereader.model.sensors.MobileSpot
import fm.pathfinder.placereader.model.Room
import fm.pathfinder.placereader.model.sensors.WifiAvailable

interface PlaceReaderListener {
    fun onRoomScanned(r: Room)
    fun onRoomStarted(r: Room)
    fun onGpsChange(gpsSpot: GpsSpot)
    fun onMobileSpotChange(mobileSpot: MobileSpot)
    fun onNewWifiDetected(wifiAvailable: List<WifiAvailable>)
}