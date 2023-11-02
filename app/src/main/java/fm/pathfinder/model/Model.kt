package fm.pathfinder.model

import java.time.LocalDateTime

data class Coordinate (
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)

data class CoordinateRoom(
    val coordinates: ArrayList<Coordinate>,
    val roomName: String
)

class GpsLocation(
    latitude: Double,
    longitude: Double,
    altitude: Double,
    accuracy: Float,
    time: Long,
    logTimestamp: LocalDateTime?
)

data class MapLine(
    val startX: Float,
    val startY: Float,
    val stopX: Float,
    val stopY: Float
)

class Room(
    val roomName: String = "Default",
    val listOfSpots: ArrayList<Spot>,
    val logTimestamp: LocalDateTime = LocalDateTime.now()
)

class Spot(
    val distance: Double,
    val movementDirection: Float,
    val wifiList: ArrayList<WifiScanResult>
) {
    private val logTimestamp: LocalDateTime = LocalDateTime.now()
    lateinit var gpsLocation: GpsLocation
}

class WifiRouter(val ssid: String,
                 val bssid: String) {

    override fun equals(other: Any?): Boolean {
        if (other is WifiRouter){
            val oth = other as WifiRouter
            return oth.ssid == this.ssid && oth.bssid == this.bssid
        }
        return super.equals(other)
    }

}

class WifiScanResult(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val timestamp: Long,
    val logTimestamp: LocalDateTime
)
