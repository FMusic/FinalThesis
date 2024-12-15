package fm.pathfinder.utils

import android.util.Log
import fm.pathfinder.model.Vector
import fm.pathfinder.model.VectorRoom
import fm.pathfinder.sensor.SensorCollector

class Building(
    private var buildingName: String = "Default"
) {
    private val shouldFilter = true
    private var scanning = false

    private val rooms = ArrayList<VectorRoom>()
    private var currentRoom = VectorRoom(ArrayList(), "Hallway")
    private val defaultRoom = currentRoom

    private val accelerationList = ArrayList<Pair<Float, Float>>()

    fun addAcceleration(acceleration: Float, direction: Float){
        accelerationList.add(Pair(acceleration, direction))
    }

    fun setScan(scanning: Boolean) {
        this.scanning = scanning
    }

    fun enterNewRoom(roomName: String) {
        Log.i(SensorCollector.TAG, "Room switch: ${currentRoom.roomName} to $roomName")
        rooms.add(currentRoom)
        currentRoom = VectorRoom(ArrayList(), roomName)
    }

    fun exitRoom() {
        Log.i(SensorCollector.TAG, "Room exit: ${currentRoom.roomName}")
        rooms.add(currentRoom)
        currentRoom = defaultRoom
    }

    //event on which other classes can subscribe to for when new point or room is added
    interface Event {
        fun onNewPoint(point: Vector)
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