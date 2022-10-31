package fm.pathfinder.model

import java.time.LocalDateTime

class WifiScanResult(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val timestamp: Long,
    val logTimestamp: LocalDateTime
)
