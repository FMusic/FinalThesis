package fm.pathfinder.model

import java.time.LocalDateTime

class Room(
    val roomName: String,
    val listOfWifiLists: ArrayList<ArrayList<WifiScanResult>>,
    val logTimestamp: LocalDateTime?
) {

}
