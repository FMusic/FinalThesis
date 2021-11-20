package fm.pathfinder.placereader.model

import fm.pathfinder.placereader.model.Passage
import fm.pathfinder.placereader.model.Room

class Building(name: String){
    var passage = Passage()
    var rooms = ArrayList<Room>()
}