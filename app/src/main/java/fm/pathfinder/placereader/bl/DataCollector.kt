package fm.pathfinder.placereader.bl

import android.content.Context
import fm.pathfinder.placereader.model.*
import fm.pathfinder.placereader.bl.scanners.GpsScanner
import fm.pathfinder.placereader.bl.scanners.TransmitterScanner
import fm.pathfinder.placereader.bl.scanners.WifiScanner
import fm.pathfinder.placereader.interfaces.PlaceReaderListener
import fm.pathfinder.placereader.model.sensors.GpsSpot
import fm.pathfinder.placereader.model.sensors.MobileSpot
import fm.pathfinder.placereader.model.sensors.WifiAvailable

class DataCollector(ctx: Context, prl: PlaceReaderListener, var building: Building):
    PlaceReaderListener {
    private val listeners = listOf(prl, this)

    private var scanners = listOf(
        GpsScanner(ctx),
        TransmitterScanner(ctx),
        WifiScanner(ctx)
    )

    private lateinit var room: Room
    private var passage = Passage()
    private lateinit var space: Space

    private lateinit var spot: Spot

    fun stopScan() {
        scanners.forEach { it.stopScan() }
    }

    fun newRoom(name: String?) = if (name == null) {
        space = passage
        scanners.forEach{
            for (listener in listeners) {
                it.addListener(listener)
            }
            it.scan()
        }
    } else {
        room = Room(name)
        space = room
    }

    //<editor-fold desc="DontUseThis">
    override fun onRoomScanned(r: Room) {
        throw IllegalAccessError("Should not access this function")
    }

    override fun onRoomStarted(r: Room) {
        throw IllegalAccessError("Should not access this function")
    }
    //</editor-fold>

    override fun onGpsChange(gpsSpot: GpsSpot) {
        checkSpot()
        if (spot.isGpsInitialized()){
            space.spots.add(spot)
            spot = Spot()
        }
        spot.gpsSpot = gpsSpot
    }

    override fun onMobileSpotChange(mobileSpot: MobileSpot) {
        checkSpot()
        if (spot.isMobileInitialized()){
            space.spots.add(spot)
            spot = Spot()
        }
        spot.mobileSpot = mobileSpot
    }

    override fun onNewWifiDetected(wifiAvailable: List<WifiAvailable>) {
        checkSpot()
        if (spot.isWifiInitialized()){
            space.spots.add(spot)
            spot = Spot()
        }
        spot.wifiAvailable = wifiAvailable
    }

    private fun checkSpot() {
        if (!::spot.isInitialized){
            spot = Spot()
        }
    }


}