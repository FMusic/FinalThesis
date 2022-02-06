package fm.pathfinder.processor

import com.google.android.gms.maps.model.IndoorBuilding
import fm.pathfinder.model.Building
import fm.pathfinder.model.GpsSpot
import fm.pathfinder.model.Room
import java.util.*

class LocationScanner {
    private val linkedGps = LinkedList<GpsSpot>()

    private val rooms = ArrayList<Room>()
    private val hallway = LinkedList<GpsSpot>()

    private lateinit var building: Building

    private var currentRoom: Room? = null
    private var roomSpots: LinkedList<GpsSpot>? = null
    private lateinit var currentSpot: GpsSpot

    fun addLocation(latitude: Double, longitude: Double, alt: Double, acc: Float, time: Long) {
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
}