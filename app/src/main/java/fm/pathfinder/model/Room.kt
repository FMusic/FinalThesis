package fm.pathfinder.model

import java.time.LocalDateTime

class Room(
    val roomName: String = "Default",
    val listOfSpots: ArrayList<Spot>,
    val logTimestamp: LocalDateTime = LocalDateTime.now()
) {

}
