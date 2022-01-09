package fm.pathfinder.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng

//
//data class MobileSpot (
//    var strengths: ArrayList<SignalStrength> = ArrayList(),
//    var locations: ArrayList<CellLocation> = ArrayList(),
//    var cells: ArrayList<CellInfo> = ArrayList()
//)

data class GpsSpot(val alt: Double, val lat: Double, val long: Double, val acc: Float, val time: Long)

//data class WifiAvailable(val ssid: String,val level: Int)

fun GpsSpot.toLatLng(): LatLng {
    return LatLng(lat, long)
}

fun Location.toLatLng(): LatLng{
    return LatLng(latitude, longitude)
}