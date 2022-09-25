package fm.pathfinder.sensors

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import fm.pathfinder.fragments.MapsFragment
import fm.pathfinder.model.*
import fm.pathfinder.presenters.MapPresenter
import java.util.*

class LocationScanner(
    private var mapsFragment: MapsFragment,
    private val mapPresenter: MapPresenter
) : LocationListener,
    BroadcastReceiver() {
    private val scanningOn = true
    private val linkedGps = LinkedList<GpsSpot>()
    private val rooms = ArrayList<Room>()
    private val hallway = LinkedList<GpsSpot>()
    private lateinit var building: Building
    private var currentRoom: Room? = null
    private var roomSpots: LinkedList<GpsSpot>? = null
    private lateinit var currentSpot: GpsSpot

    private val wifiManager =
        mapsFragment.activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager

    override fun onLocationChanged(location: Location) {
        Log.i(TAG, location.toString())
        mapsFragment.changeLocation(location.toLatLng())
        if (scanningOn)
            addLocation(
                location.latitude, location.longitude,
                location.altitude, location.accuracy, location.time
            )
    }

    private fun addLocation(
        latitude: Double,
        longitude: Double,
        alt: Double,
        acc: Float,
        time: Long
    ) {
        val gpsSpot = GpsSpot(alt, latitude, longitude, acc, time)
        linkedGps.add(gpsSpot)
        currentSpot = gpsSpot
        roomSpots?.add(gpsSpot) ?: hallway.add(gpsSpot)
    }

    fun extractData(): Building {
        building = Building(rooms, hallway)
        return building
    }

    fun enterNewRoom(roomName: String) {
        roomSpots = LinkedList()
        currentRoom = Room(roomName, currentSpot, roomSpots ?: return)
    }

    fun exitRoom(): String? {
        val roomName = currentRoom?.roomName
        currentRoom?.let {
            it.spots = roomSpots ?: throw Exception("Room spots are not initialized")
            rooms.add(it)
        }
        currentRoom = null
        return roomName
    }

    companion object {
        private const val TAG: String = "LocationScanner"
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val succ = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        var scanResults = ArrayList<WifiResult>()
        val scanRes2 = ArrayList<ScanResult>()
        if (succ) {
            for (x in wifiManager.scanResults) {
                scanResults.add(
                    WifiResult(
                        x.BSSID,
                        x.SSID,
                        x.capabilities,
                        x.frequency,
                        x.level,
                        x.timestamp
                    )
                )
                mapsFragment.logWifi(x)
                Log.d("WifiRes", "New wifi result:${x.BSSID}. ${x.capabilities}")
            }
            mapPresenter.initNewRangingRequest(wifiManager.scanResults)

        }
    }
}