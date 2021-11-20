package fm.pathfinder.placereader

import android.content.Context
import fm.pathfinder.placereader.bl.DataCollector
import fm.pathfinder.placereader.interfaces.PlaceReaderListener
import fm.pathfinder.placereader.model.Building

class PlaceReader(ctx: Context, prl: PlaceReaderListener, buildingName: String) {
    var building = Building(buildingName)
    var dataCollector = DataCollector(ctx, prl, building)

    fun startScan() {
        newRoom(null)
    }

    fun stopScan() {
        dataCollector.stopScan();
    }

    fun newRoom(name: String?) {
        dataCollector.newRoom(name)
    }
}