package fm.pathfinder.processor

import com.google.android.gms.maps.model.IndoorBuilding
import fm.pathfinder.model.Building
import fm.pathfinder.model.GpsSpot
import java.util.*

class LocationScanner {
    private val linkedGps = LinkedList<GpsSpot>()

    fun addLocation(latitude: Double, longitude: Double, alt: Double, acc: Float, time: Long) {
        linkedGps.add(GpsSpot(alt, latitude, longitude, acc, time))
    }

    fun extractData(): Building {
        return Building()
    }
}