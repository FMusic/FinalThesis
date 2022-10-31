package fm.pathfinder.model

import java.time.LocalDateTime

class Spot(
    val wifiList: List<WifiScanResult>,
    val gpsLocation: GpsLocation,
    val movementDirection: Float,
    val logTimestamp: LocalDateTime = LocalDateTime.now()
)
