package fm.pathfinder.utils

import android.util.Log
import fm.pathfinder.model.Coordinate
import fm.pathfinder.model.CoordinateRoom
import fm.pathfinder.sensors.LocationScanner

class Building (
    private var buildingName: String="Default"
) {
    private val shouldFilter = true

    private val rooms = ArrayList<CoordinateRoom>()
    private val kalmanFilter = Kalman()
    private var currentRoom = CoordinateRoom(ArrayList(), "Hallway")
    private val defaultRoom = currentRoom

    fun addPoint(point: Coordinate) {
        if (shouldFilter) {
            val filteredPoint = kalmanFilter.predict(point)
            currentRoom.coordinates.add(filteredPoint)
        } else {
            currentRoom.coordinates.add(point)
        }
    }

    fun enterNewRoom(roomName: String) {
        Log.i(LocationScanner.TAG, "Room switch: ${currentRoom.roomName} to $roomName")
        rooms.add(currentRoom)
        currentRoom = CoordinateRoom(ArrayList(), roomName)
    }

    fun exitRoom(){
        Log.i(LocationScanner.TAG, "Room exit: ${currentRoom.roomName}")
        rooms.add(currentRoom)
        currentRoom = defaultRoom
    }

    //event on which other classes can subscribe to for when new point or room is added
    interface Event {
        fun onNewPoint(point: Coordinate)
        fun onNewRoom(roomName: String)
    }

    // subscribers to event
    private val subscribers = ArrayList<Event>()
    //add subscriber
    fun subscribe(subscriber: Event) {
        subscribers.add(subscriber)
    }

    fun setName(name: String) {
        buildingName = name
    }

}