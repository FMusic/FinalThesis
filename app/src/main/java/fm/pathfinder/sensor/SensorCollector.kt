package fm.pathfinder.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import fm.pathfinder.model.Building
import fm.pathfinder.model.ErrorState
import fm.pathfinder.ui.MapPresenter
import fm.pathfinder.utils.AdaptiveKalmanFilter
import fm.pathfinder.utils.Calibrator
import fm.pathfinder.utils.ZUPTFilter
import java.sql.Timestamp

class SensorCollector(
    private val context: Context,
    private val building: Building,
    private val mapPresenter: MapPresenter
) : SensorEventListener {
    private val samplingPeriod = SensorManager.SENSOR_DELAY_UI
    private val calibrator = Calibrator()

    private var sensorsInitialized = false
    private var calibrationMode = false
    private var scanMode = false

    private var startTimestamp = 0L

    private lateinit var kalmanFilter: AdaptiveKalmanFilter

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                rotationValues(event.values, processTimestamp(event.timestamp))
            }

            Sensor.TYPE_ACCELEROMETER -> {
                accelerationValues(event.values, processTimestamp(event.timestamp))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "Accuracy changed")
    }

    fun initializeSensors() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotationInitialized = sensorManager.registerListener(this, sensor, samplingPeriod)
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerationInitialized = sensorManager.registerListener(this, sensor, samplingPeriod)
        sensorsInitialized = rotationInitialized && accelerationInitialized
    }

    fun setCalibration(value: Boolean) {
        calibrationMode = value
        if (!sensorsInitialized) {
            initializeSensors()
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

    private fun rotationValues(values: FloatArray, timestamp: Long) {
        if (!sensorsInitialized || !(calibrationMode && scanMode)) return
    }

    private fun accelerationValues(values: FloatArray, timestamp: Long) {
        if (!sensorsInitialized) return
        else if (calibrationMode) {
            val ts = Timestamp(timestamp)
            calibrator.addAcceleration(values, ts)
        } else if (scanMode) {
            if (ZUPTFilter.detectZeroVelocity(values)) {
                val currentErrorState = ErrorState()
                kalmanFilter.updateErrorStateWithZUPT(
                    currentErrorState,
                    event.values,
                    deltaTime
                )
            }
        }
    }

    private fun processTimestamp(timestamp: Long): Long {
        if (startTimestamp == 0L) {
            startTimestamp = timestamp
        }
        return (timestamp - startTimestamp) / 1_000_000
    }

    companion object {
        const val TAG = "Location"
        const val ORIENTATION_MATRIX_SIZE = 3
        const val MINIMUM_ACCELERATION_DELTA = 0.5f
    }
}

