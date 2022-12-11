package fm.pathfinder.model

import java.time.LocalDateTime

class Spot(
    val distance: Double,
    val movementDirection: Float,
    val wifiList: ArrayList<WifiScanResult>
) {
    private val logTimestamp: LocalDateTime = LocalDateTime.now()
    lateinit var gpsLocation: GpsLocation
}