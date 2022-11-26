package fm.pathfinder.collecting

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import fm.pathfinder.MainActivity
import fm.pathfinder.conf.LocalDateTimeSerializer
import fm.pathfinder.collecting.wifi.WifiProcessor
import fm.pathfinder.data.FileManager
import fm.pathfinder.fragments.MapsFragment
import java.time.LocalDateTime

class MapPresenter(
    private val mapsFragment: MapsFragment,
    private val context: Context
) {
    companion object {
        private const val TAG: String = "MapPresenter"
    }

    private lateinit var wifiProcessor: WifiProcessor
    private lateinit var gpsProcessor: GpsService
    private var scanningOn = false
    private val samplingPeriod = SensorManager.SENSOR_DELAY_GAME

    private var locationCollector = LocationCollector(mapsFragment::logIt)

    init {
        try {
            gpsProcessor = GpsService(context, mapsFragment::logIt, locationCollector)
            val mSensorManager =
                context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerationProcessor =
                OrientationProcessor(context, locationCollector::angleChange)
            listOf(Sensor.TYPE_ROTATION_VECTOR).forEach {
                val sensor = mSensorManager.getDefaultSensor(it)
                mSensorManager.registerListener(accelerationProcessor, sensor, samplingPeriod)
            }
            val velocityProcessor =
                VelocityProcessor(
                    context,
                    locationCollector::distance,
                    accelerationProcessor::getVelocityOrientationMatrix
                )
            listOf(Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_ACCELEROMETER).forEach {
                val sensor = mSensorManager.getDefaultSensor(it)
                mSensorManager.registerListener(velocityProcessor, sensor, samplingPeriod)
            }
            wifiProcessor = WifiProcessor(context, locationCollector)
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
        FileManager(context).storeBuildingToFile(locationCollector.extractData())
        Toast.makeText(context, "Saving building data", Toast.LENGTH_SHORT).show()
    }

    fun newRoom(roomName: String) {
        locationCollector.enterNewRoom(roomName)
        Toast.makeText(context, "New Room enter $roomName", Toast.LENGTH_SHORT).show()
    }

    fun exitRoom() {
        val roomName = locationCollector.exitRoom()
        Toast.makeText(context, "Room exited $roomName", Toast.LENGTH_SHORT).show()
    }


}