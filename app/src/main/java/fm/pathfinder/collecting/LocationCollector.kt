package fm.pathfinder.collecting

import android.location.Location
import android.net.wifi.ScanResult
import android.util.Log
import fm.pathfinder.model.GpsLocation
import fm.pathfinder.model.Room
import fm.pathfinder.model.Spot
import fm.pathfinder.model.WifiScanResult
import fm.pathfinder.model.tpls.WifiResultTuple
import java.time.LocalDateTime
import kotlin.math.abs

class LocationCollector(
    private val logFunc: (String) -> Unit
) {
    private val locations = ArrayList<GpsLocation>()
    private val rooms = HashSet<Room>()
    private val uniqueWifiRouters = HashSet<String>()
    val uniqueWifiRoutersScanResults = ArrayList<ScanResult>()
    private val locationsByRoom = HashMap<Room, ArrayList<Location>>()

    var currentRoom: Room = Room("first", ArrayList(), LocalDateTime.now())
    private var lastKnownLocation = GpsLocation(.0, .0, .0, .0f, 0L, LocalDateTime.now())

    private var directionDegrees = 0F

    private var distanceSum = 0F


    init {
        rooms.add(currentRoom)
    }

    fun enterNewRoom(roomName: String) {
        rooms.add(currentRoom)
        val room = Room(roomName, ArrayList(), LocalDateTime.now())
        currentRoom = room
        locationsByRoom[room] = ArrayList()
    }

    fun addLocation(location: Location) {
        val currentLocation = GpsLocation(
            location.latitude,
            location.longitude,
            location.altitude,
            location.accuracy,
            location.time,
            LocalDateTime.now()
        )
        lastKnownLocation = currentLocation
        locations.add(currentLocation)
        locationsByRoom[currentRoom]?.add(location)
    }

    fun extractData(): Any {
        rooms.add(currentRoom)
        Log.i(TAG, "returning data: " + (rooms.iterator().next().listOfSpots[0].wifiList[0].toString()))
        return rooms;
    }

    fun exitRoom(): String {
        return "why"
    }

    fun addWifiSpots(wifiResults: List<ScanResult>): WifiResultTuple {
        val uniqueWifiResults = ArrayList<ScanResult>()
        val wifiScanResults = ArrayList<WifiScanResult>()
        val wifiScanResFinal = ArrayList<ScanResult>()
        wifiResults.forEach {
            val isAdded = uniqueWifiRouters.add(it.BSSID)
            if (isAdded) {
                uniqueWifiResults.add(it)
            }
            val wifiScanResult = WifiScanResult(it.SSID, it.BSSID, it.level, it.timestamp, LocalDateTime.now())
            wifiScanResults.add(wifiScanResult)
            wifiScanResFinal.add(it)
        }
        currentRoom.listOfSpots.add(Spot(wifiScanResults, lastKnownLocation, directionDegrees))
        uniqueWifiRoutersScanResults.addAll(wifiScanResFinal)
        return WifiResultTuple(uniqueWifiResults, wifiScanResFinal)
    }

    fun angleChange(degrees: Float){
        if (abs(degrees - directionDegrees) > 30){
            directionDegrees = degrees
            logFunc("New degrees: $directionDegrees\n")
            Log.i("Deg", "Degrees: $directionDegrees")
        }
    }

    fun startScan() {

    }

    fun distance(distance: Float){
        distanceSum += distance
        Log.i("dist", "DISTANCE: $distance, sum=$distanceSum")
    }

    companion object {
        const val TAG = "Location"
    }
}

