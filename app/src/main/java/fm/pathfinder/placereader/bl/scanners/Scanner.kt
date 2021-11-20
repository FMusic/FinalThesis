package fm.pathfinder.placereader.bl.scanners

import fm.pathfinder.placereader.interfaces.PlaceReaderListener


abstract class Scanner{
    var listeners = ArrayList<PlaceReaderListener>()
    var shouldScan = false

    open fun scan(){
        shouldScan = true
    }

    open fun stopScan(){
        shouldScan = false
    }

    open fun addListener(listener: PlaceReaderListener){
        listeners.add(listener)
    }
}