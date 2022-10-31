package fm.pathfinder.collecting

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import fm.pathfinder.Constants
import fm.pathfinder.MainActivity
import fm.pathfinder.conf.LocalDateTimeSerializer
import fm.pathfinder.collecting.wifi.WifiProcessor
import java.time.LocalDateTime

class MapPresenter(
    private val mapsFragment: MapsFragment,
    private val mainActivity: MainActivity
) {
    companion object {
        private const val TAG: String = "MapPresenter"
    }

    private lateinit var wifiProcessor: WifiProcessor
    private lateinit var sensorsProcessor: SensorsProcessor
    private lateinit var gpsProcessor: GpsService
    private var scanningOn = false
    private val samplingPeriod = Constants.SLEEP_TIME_MS.times(1000).toInt()

    private var locationCollector = LocationCollector(mapsFragment::logIt)

    init {
        try {
            gpsProcessor = GpsService(mapsFragment, locationCollector)
            val mSensorManager =
                mainActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerationProcessor =
                OrientationProcessor(mapsFragment.context, locationCollector::angleChange)
            listOf(Sensor.TYPE_ROTATION_VECTOR).forEach {
                val sensor = mSensorManager.getDefaultSensor(it)
                mSensorManager.registerListener(accelerationProcessor, sensor, samplingPeriod)
            }
            val velocityProcessor =
                VelocityProcessor(mapsFragment.context, locationCollector::distance)
            listOf(Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ACCELEROMETER).forEach {
                val sensor = mSensorManager.getDefaultSensor(it)
                mSensorManager.registerListener(velocityProcessor, sensor, samplingPeriod)
            }
            wifiProcessor = WifiProcessor(mapsFragment, locationCollector)
        } catch (e: SecurityException) {
            Log.e(TAG, "SECURITY, NO WIFI WILL BE AVAILABLE")
            e.printStackTrace()
        }
    }


    fun startScan() {
        scanningOn = true
        locationCollector.startScan()
        wifiProcessor.startScan()
    }

    fun stopScan() {
        scanningOn = false
        val buildingData = locationCollector.extractData()
        val gsonBuilder =
            GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        val gson = gsonBuilder.setPrettyPrinting().create()
        val jsonBuilding = gson.toJson(buildingData)
        mainActivity.createFile(jsonBuilding)
        Toast.makeText(mapsFragment.context, "Saving building data", Toast.LENGTH_SHORT).show()
    }

    fun newRoom(roomName: String) {
        locationCollector.enterNewRoom(roomName)
        Toast.makeText(mapsFragment.context, "New Room enter $roomName", Toast.LENGTH_SHORT).show()
    }

    fun exitRoom() {
        val roomName = locationCollector.exitRoom()
        Toast.makeText(mapsFragment.context, "Room exited $roomName", Toast.LENGTH_SHORT).show()
    }


}