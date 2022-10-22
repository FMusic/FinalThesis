package fm.pathfinder.collecting

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import fm.pathfinder.Constants
import fm.pathfinder.MainActivity
import fm.pathfinder.collecting.sensors.WifiProcessor

@SuppressLint("MissingPermission")
class MapPresenter(
    private val mapsFragment: MapsFragment,
    private val mainActivity: MainActivity
) : LocationListener {
    companion object {
        private const val TAG: String = "MapPresenter"
    }

    private lateinit var wifiProcessor: WifiProcessor
    private lateinit var sensorsProcessor: SensorsProcessor
    private var scanningOn = false

    private var locationScanner = LocationScanner(this)


    init {
        try {
            initLocationRequests()
            val mSensorManager =
                mainActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorsProcessor = SensorsProcessor(mSensorManager, mapsFragment, locationScanner)
            wifiProcessor = WifiProcessor(mapsFragment, locationScanner)
        } catch (e: SecurityException) {
            Log.e(TAG, "SECURITY, NO WIFI WILL BE AVAILABLE")
            e.printStackTrace()
        }
    }


    private fun initLocationRequests() {
        val locationManager =
            mapsFragment.activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            Constants.SLEEP_TIME_MS,
            Constants.MIN_DISTANCE_FEET,
            this
        )
    }

    fun startScan() {
        scanningOn = true
        wifiProcessor.startScan()
    }

    fun stopScan() {
        scanningOn = false
        val buildingData = locationScanner.extractData()
        val jsonBuilding = Gson().toJson(buildingData)
        mainActivity.createFile(jsonBuilding)
        Toast.makeText(mapsFragment.context, "Saving building data", Toast.LENGTH_SHORT).show()
    }

    fun newRoom(roomName: String) {
        locationScanner.enterNewRoom(roomName)
        Toast.makeText(mapsFragment.context, "New Room enter $roomName", Toast.LENGTH_SHORT).show()
    }

    fun exitRoom() {
        val roomName = locationScanner.exitRoom()
        Toast.makeText(mapsFragment.context, "Room exited $roomName", Toast.LENGTH_SHORT).show()
    }


    override fun onLocationChanged(location: Location) {
        Log.i(TAG, "Location/GPS: $location")
        mapsFragment.logIt("GPS: ${location.longitude}, ${location.latitude}")
        mapsFragment.changeLocation(LatLng(location.latitude, location.longitude))
        locationScanner.addLocation(location)
    }


}