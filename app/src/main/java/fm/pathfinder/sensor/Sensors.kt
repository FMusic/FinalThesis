package fm.pathfinder.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import fm.pathfinder.model.Building
import fm.pathfinder.utils.Calibrator

class Sensors(
    private val context: Context,
    private val building: Building
) : SensorEventListener {
    private val calibrator = Calibrator()
    private lateinit var sensorCollector: SensorCollector

    private var sensorsInitialized = false
    private var calibrationMode = false
    private var scanMode = false

    private var startTimestamp = 0L

    init {
        initializeSensors()
    }

    private fun initializeSensors() {
        Log.i(TAG, "Initializing sensors")
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotationInitialized = sensorManager.registerListener(this, sensor, SAMPLING_PERIOD)
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerationInitialized = sensorManager.registerListener(this, sensor, SAMPLING_PERIOD)
        sensorsInitialized = rotationInitialized && accelerationInitialized
        Log.i(TAG, "Sensors initialized: $sensorsInitialized")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (!scanMode && !calibrationMode) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                if(!calibrationMode){
                    sensorCollector.rotationValues(event.values, processTimestamp(event.timestamp))
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                if (calibrationMode) {
                    calibrator.addAcceleration(event.values, processTimestamp(event.timestamp))
                } else  {
                    sensorCollector.accelerationValues(event.values, processTimestamp(event.timestamp))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Accuracy changed")
    }

    fun setCalibration(value: Boolean) {
        calibrationMode = value
        if (!sensorsInitialized) {
            initializeSensors()
        }
        if (!value) {
            val stride = calibrator.stepLength(5).toFloat()
            Log.i(TAG, "Calibrator Stride length: $stride")
            sensorCollector = SensorCollector(stride)
        }
    }

    fun setScan(scanning: Boolean) {
        scanMode = scanning
        if (!sensorsInitialized && scanning) {
            initializeSensors()
        }
        if (!scanning) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(this)
        }
    }

    /**
     * Converts sensor timestamp to milliseconds relative to start.
     */
    private fun processTimestamp(timestamp: Long): Long {
        if (startTimestamp == 0L) {
            startTimestamp = timestamp
        }
        return (timestamp - startTimestamp) / 1_000_000  // convert to milliseconds
    }

    companion object {
        const val TAG = "SensorCollector"
        const val ORIENTATION_MATRIX_SIZE = 3
        const val MINIMUM_ACCELERATION_DELTA = 0.5f
        const val SAMPLING_PERIOD = SensorManager.SENSOR_DELAY_UI
    }
}

