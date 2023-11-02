package fm.pathfinder.sensors

import android.content.Context
import android.location.Location
import android.net.wifi.ScanResult
import android.util.Log
import fm.pathfinder.model.GpsLocation
import fm.pathfinder.model.Room
import fm.pathfinder.model.Spot
import fm.pathfinder.model.WifiScanResult
import java.time.LocalDateTime
import kotlin.math.abs

class LocationScanner(
    private val context: Context
) {
    private var scanning = false
    private val uniqueWifiRouters = HashSet<String>()
    val allWifiRouters = ArrayList<ScanResult>()
    private val locationsByRoom = HashMap<Room, ArrayList<Location>>()
    private val defaultRoom = Room("Hallway", ArrayList(), LocalDateTime.now())

    var currentRoom: Room = defaultRoom
    private lateinit var lastKnownLocation: GpsLocation
    private var directionDegrees = 0F
    private var distanceSum = 0.0

    private var movementDirection: Float = 0F
    private var lastWifisScanned = ArrayList<WifiScanResult>()
    private var currentSpot: Spot = Spot(0.0, movementDirection, lastWifisScanned)


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
        lastWifisScanned = wifiScanResults
        return wifiScanResFinal
    }

    fun angleChange(degrees: Float) {
        if (abs(degrees - directionDegrees) > 30) {
            directionDegrees = degrees
            movementDirection = degrees
        }
    }

    fun startScan() {
        scanning = true
        distanceSum = 0.0
    }

    fun distance(distance: Double) {
        Log.i(
            TAG,
            "SPOT: dist: ${currentSpot.distance} angle: ${currentSpot.movementDirection} " +
                    "wifis: ${if (currentSpot.wifiList.isNotEmpty()) "FULL" else "EMPTY"}"
        )
        logFunc("Distance: ${currentSpot.distance}, Angle: ${currentSpot.movementDirection}\n")
        distanceSum += distance
        if (scanning) {
            currentRoom.listOfSpots.add(currentSpot)
            currentSpot = Spot(
                distance = distanceSum,
                movementDirection = movementDirection,
                wifiList = lastWifisScanned
            )
            lastWifisScanned = ArrayList()
        }
    }

    companion object {
        const val TAG = "Location"
    }
}

