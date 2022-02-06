package fm.pathfinder.model

data class Building(
    val rooms: List<Room>,
    val hallway: List<GpsSpot>
)

data class Room(
    val roomName: String,
    val entrySpot: GpsSpot,
    var spots: List<GpsSpot>
)
