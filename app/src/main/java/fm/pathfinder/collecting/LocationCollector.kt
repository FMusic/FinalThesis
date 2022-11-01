package fm.pathfinder.collecting

import android.location.Location
import android.net.wifi.ScanResult
import android.util.Log
import fm.pathfinder.model.GpsLocation
import fm.pathfinder.model.Room
import fm.pathfinder.model.Spot
import fm.pathfinder.model.WifiScanResult
import java.time.LocalDateTime
import kotlin.math.abs

class LocationCollector(
    private val logFunc: (String) -> Unit
) {
    private val locations = ArrayList<GpsLocation>()
    private val rooms = HashSet<Room>()
    private val uniqueWifiRouters = HashSet<String>()
    val allWifiRouters = ArrayList<ScanResult>()
    private val locationsByRoom = HashMap<Room, ArrayList<Location>>()

    var currentRoom: Room = Room("Hallway", ArrayList(), LocalDateTime.now())
    private lateinit var lastKnownLocation: GpsLocation
    private var directionDegrees = 0F
    private var distanceSum = 0F

    private var currentSpot: Spot = Spot(0F)


    init {
        rooms.add(currentRoom)
    }

    fun enterNewRoom(roomName: String) {
        Log.i(TAG, "Room switch: ${currentRoom.roomName} to $roomName")
        rooms.add(currentRoom)
        currentRoom = Room(roomName, ArrayList(), LocalDateTime.now())
        locationsByRoom[currentRoom] = ArrayList()
    }

    fun addLocation(location: Location) {
//        val currentLocation = GpsLocation(
//            location.latitude,
//            location.longitude,
//            location.altitude,
//            location.accuracy,
//            location.time,
//            LocalDateTime.now()
//        )
//        lastKnownLocation = currentLocation
//        locations.add(currentLocation)
//        locationsByRoom[currentRoom]?.add(location)
    }

    fun extractData(): Any {
        currentRoom.listOfSpots.add(currentSpot)
        rooms.add(currentRoom)
        Log.i(TAG, "Extracting data..")
        return rooms;
    }

    fun exitRoom(): String {
        return "why"
    }

    fun addWifiSpots(wifiResults: List<ScanResult>): ArrayList<ScanResult> {
        val wifiScanResults = ArrayList<WifiScanResult>()
        val wifiScanResFinal = ArrayList<ScanResult>()
        wifiResults.forEach {
            if (uniqueWifiRouters.add(it.BSSID)) {
                wifiScanResFinal.add(it)
            }
            wifiScanResults.add(
                WifiScanResult(
                    it.SSID,
                    it.BSSID,
                    it.level,
                    it.timestamp,
                    LocalDateTime.now()
                )
            )
        }
        Log.i(TAG, "WIFI Results are here")
        allWifiRouters.addAll(wifiScanResFinal)
        currentSpot.wifiList.addAll(wifiScanResults)
        return wifiScanResFinal
    }

    fun angleChange(degrees: Float) {
        if (abs(degrees - directionDegrees) > 30) {
            directionDegrees = degrees
            logFunc("New degrees: $directionDegrees\n")
            Log.i(TAG, "Degrees: $directionDegrees")
        }
        currentSpot.movementDirection = degrees
    }

    fun startScan() {

    }

    fun distance(distance: Float) {
        Log.i(
            TAG, "finishing: " +
                    "dist: ${currentSpot.distance} " +
                    "angle: ${currentSpot.movementDirection} " +
                    "wifis: ${if (currentSpot.wifiList.isNotEmpty()) "FULL" else "EMPTY"}"
        )
        currentRoom.listOfSpots.add(currentSpot)
        distanceSum += distance
        currentSpot = Spot(distanceSum)
    }

    companion object {
        const val TAG = "Location"
    }
}
