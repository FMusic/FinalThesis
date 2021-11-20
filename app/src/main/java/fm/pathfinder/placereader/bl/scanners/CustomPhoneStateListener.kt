package fm.pathfinder.placereader.bl.scanners

import android.content.Context
import android.telephony.CellInfo
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import fm.pathfinder.placereader.interfaces.PlaceReaderListener
import fm.pathfinder.placereader.model.sensors.MobileSpot

class CustomPhoneStateListener(var ctx: Context) : PhoneStateListener() {
    private var listeners = ArrayList<PlaceReaderListener>()
    var ms: MobileSpot? = null
    var memoryOn = false

    override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
        super.onCellInfoChanged(cellInfo)
        cellInfo?.stream()?.map { x -> ms?.cells?.add(x) }
        if (ms != null){
            listeners.forEach{
                it.onMobileSpotChange(ms!!)
            }
        }
    }

    override fun onCellLocationChanged(location: CellLocation?) {
        super.onCellLocationChanged(location)
        if (location != null) {
            if (ms != null){
                ms?.locations?.add(location)
                listeners.forEach { it.onMobileSpotChange(ms!!) }
            }
        }
    }

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
        super.onSignalStrengthsChanged(signalStrength)
        if (signalStrength != null) {
            if (ms != null){
                ms?.strengths?.add(signalStrength)
                listeners.forEach { it.onMobileSpotChange(ms!!) }
            }
        }
    }

    fun addListener(listener: PlaceReaderListener) {
        listeners.add(listener)
    }
}
