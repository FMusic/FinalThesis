package fm.pathfinder.placereader.bl.scanners

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import fm.pathfinder.placereader.model.sensors.GpsSpot
import java.lang.Exception

class GpsScanner(ctx: Context) : Scanner() {
    var mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
    var mSettingsClient = LocationServices.getSettingsClient(ctx)

    init {
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw Exception("Don't have permissions")
        } else {
            mFusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    process( it.altitude, it.latitude, it.longitude)
                }
            }
        }
    }

    private fun process(altitude: Double, latitude: Double, longitude: Double) {
        if (shouldScan ) {
            val gpsSpot = GpsSpot(altitude, latitude, longitude)
            listeners.forEach { it.onGpsChange(gpsSpot) }
        }
    }

}
