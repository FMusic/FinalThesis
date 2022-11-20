package fm.pathfinder.model

import java.time.LocalDateTime

class Spot(
    val distance: Double
) {
    private val logTimestamp: LocalDateTime = LocalDateTime.now()
    val wifiList: ArrayList<WifiScanResult> = ArrayList()
    lateinit var gpsLocation: GpsLocation
    var movementDirection: Float = 0.0F

}