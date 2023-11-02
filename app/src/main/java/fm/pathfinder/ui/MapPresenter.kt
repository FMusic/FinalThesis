package fm.pathfinder.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import fm.pathfinder.model.Coordinate
import fm.pathfinder.sensors.GpsSensor
import fm.pathfinder.sensors.LocationScanner
import fm.pathfinder.sensors.RotationSensor
import fm.pathfinder.sensors.AccelerationSensor
import fm.pathfinder.sensors.WifiSensor
import fm.pathfinder.utils.Building
import javax.inject.Inject

class MapPresenter(
    private val mapFragment: MapFragment,
    private val context: Context
): Building.Event{
    companion object {
        private const val TAG: String = "MapPresenter"
    }

    private lateinit var wifiSensor: WifiSensor
    private lateinit var gpsProcessor: GpsSensor
    private lateinit var rotationSensor: RotationSensor
    private lateinit var accelerationSensor: AccelerationSensor
    private var scanningOn = false
    private val samplingPeriod = SensorManager.SENSOR_DELAY_UI

    private var locationScanner = LocationScanner(context)
    private val building: Building = Building()

    init {
        building.subscribe(this)
        try {
            gpsProcessor = GpsSensor(context, mapFragment::logIt, locationScanner)
            rotationSensor = RotationSensor(context, locationScanner::angleChange)
            accelerationSensor = AccelerationSensor(context, locationScanner::distance)
            wifiSensor = WifiSensor(context, locationScanner)

            val mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

            var sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            mSensorManager.registerListener(rotationSensor, sensor, samplingPeriod)

            listOf(
                Sensor.TYPE_LINEAR_ACCELERATION
//                , Sensor.TYPE_ACCELEROMETER
            ).forEach {
                sensor = mSensorManager.getDefaultSensor(it)
                mSensorManager.registerListener(accelerationSensor, sensor, samplingPeriod)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SECURITY, NO WIFI WILL BE AVAILABLE")
            e.printStackTrace()
        }
    }


    fun startScan() {
        scanningOn = true
        locationScanner.startScan()
        wifiSensor.startScan()
    }

    fun stopScan(filename: String) {
        scanningOn = false
        DataStoragePresenter(context).storeBuildingToFile(locationScanner.extractData(), filename)
        Toast.makeText(context, "Saving building data", Toast.LENGTH_SHORT).show()
    }

    fun newRoom(roomName: String) {
        locationScanner.enterNewRoom(roomName)
        Toast.makeText(context, "New Room enter $roomName", Toast.LENGTH_SHORT).show()
    }

    fun exitRoom() {
        val roomName = locationScanner.exitRoom()
        Toast.makeText(context, "Room exited $roomName", Toast.LENGTH_SHORT).show()
    }

    override fun onNewPoint(point: Coordinate) {
        TODO("Not yet implemented")
    }

    override fun onNewRoom(roomName: String) {
        TODO("Not yet implemented")
    }


}