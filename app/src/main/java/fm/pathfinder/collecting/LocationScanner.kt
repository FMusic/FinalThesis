package fm.pathfinder.collecting

import android.location.Location
import android.net.wifi.ScanResult
import com.google.gson.JsonElement
import fm.pathfinder.model.GpsLocation
import fm.pathfinder.model.Room
import fm.pathfinder.model.WifiScanResult
import fm.pathfinder.model.tpls.WifiResultTuple
import java.time.LocalDateTime

class LocationScanner(
    private var mapPresenter: MapPresenter
) {
    private val locations = ArrayList<GpsLocation>()
    private val rooms = ArrayList<Room>()
    private val uniqueWifiRouters = HashSet<String>()
    val uniqueWifiRoutersScanResults = ArrayList<ScanResult>()
    private val locationsByRoom = HashMap<Room, ArrayList<Location>>()

    var currentRoom: Room = Room("default", ArrayList(), LocalDateTime.now())

    fun enterNewRoom(roomName: String) {
        val room = Room(roomName, ArrayList(), LocalDateTime.now())
        currentRoom = room
        rooms.add(room)
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
        locations.add(currentLocation)
        locationsByRoom[currentRoom]?.add(location)
    }

    fun extractData(): JsonElement? {
        TODO("Not yet implemented")

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

            wifiScanResults.add(
                WifiScanResult(
                    it.SSID,
                    it.BSSID,
                    it.level,
                    it.timestamp,
                    LocalDateTime.now()
                )
            )
            wifiScanResFinal.add(it)
        }
        currentRoom.listOfWifiLists.add(wifiScanResults)
        uniqueWifiRoutersScanResults.addAll(wifiScanResFinal)
        return WifiResultTuple(uniqueWifiResults, wifiScanResFinal)
    }
}

