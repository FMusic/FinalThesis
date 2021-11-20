package fm.pathfinder.placereader.model.sensors

import android.telephony.CellInfo
import android.telephony.CellLocation
import android.telephony.CellSignalStrength
import android.telephony.SignalStrength

class MobileSpot {
    var strengths: ArrayList<SignalStrength> = ArrayList()
    var locations: ArrayList<CellLocation> = ArrayList()
    var cells: ArrayList<CellInfo> = ArrayList()
}
