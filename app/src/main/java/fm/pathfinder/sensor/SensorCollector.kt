package fm.pathfinder.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import fm.pathfinder.model.Acceleration
import fm.pathfinder.model.Azimuth
import fm.pathfinder.ui.MapPresenter
import fm.pathfinder.utils.Building
import kotlin.math.pow

class SensorCollector(
    private val context: Context,
    private val building: Building,
    private val mapPresenter: MapPresenter
) {

    private lateinit var wifiSensor: WifiSensor
    private lateinit var gpsProcessor: GpsSensor
    private lateinit var rotationSensor: RotationSensor
    private lateinit var accelerationSensor: AccelerationSensor
    private var scanningOn = false
    private val samplingPeriod = SensorManager.SENSOR_DELAY_UI
    private var lastAcceleration: Acceleration? = null
    private var timestampBegin: Long = 0
    private var maxAcceleration = Acceleration(0f, 0f, 0f)

    private var lastAzimuth: Float = 0f
    private var lastAccelerometerReading: FloatArray? = null
    private var lastMagnetometerReading: FloatArray? = null


    private fun initSensors() {
//        gpsProcessor = GpsSensor(context, building)
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        rotationSensor = RotationSensor(this)
        sensorManager.registerListener(rotationSensor,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            samplingPeriod)

        accelerationSensor = AccelerationSensor(this)
        sensorManager.registerListener(
            accelerationSensor,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            samplingPeriod
        )

//        wifiSensor = WifiSensor(context, this)

        lastAzimuth = 0f
        lastAccelerometerReading = null
        lastMagnetometerReading = null
    }

    fun setScan(scanning: Boolean) {
        if (scanning) {
            initSensors()
        }
        scanningOn = scanning
    }

    fun collectAzimuth(azimuth: Azimuth) {
        if (scanningOn) {
            lastAzimuth = azimuth.degrees
            mapPresenter.orientation(azimuth.degrees)
        }
    }

    private var stepPauseCounter = 0

    fun collectAcceleration(acceleration: Acceleration) {
        building.addAcceleration(acceleration.norm(), lastAzimuth)
        if (scanningOn && acceleration.norm() > MINIMUM_ACCELERATION_DELTA) {
            Log.i(TAG, "AccelerationNorm: ${acceleration.norm()}")
            if (lastAcceleration == null) {
                lastAcceleration = acceleration
                maxAcceleration = acceleration
                timestampBegin = System.currentTimeMillis()
                stepPauseCounter = 0
            } else {
                mapPresenter.acceleration(acceleration)
//                kalman
                val delta = acceleration.norm() - maxAcceleration.norm()
                if (delta > 0f) {
                    // val raste
                    maxAcceleration = acceleration
                } else {
                    // val pada
                    stepPauseCounter++
                    if (stepPauseCounter == 1) {
                        addStep()
                        lastAcceleration = null
                    }
                }
            }
        }
    }

    private fun addStep() {
        val time = (System.currentTimeMillis() - timestampBegin).toFloat()
        val speed =
            (maxAcceleration.norm() / 2) * time.pow(2)
        val distance = speed * time
        maxAcceleration = Acceleration(0f, 0f, 0f)
        Log.i(TAG, "Step: $distance, $lastAzimuth")
        mapPresenter.newStep(distance)
    }

    fun collectMagnetometer(values: FloatArray) {
        lastMagnetometerReading = values
        computeOrientation()
    }

    fun collectAccelerometer(values: FloatArray) {
        lastAccelerometerReading = values
    }

    private fun computeOrientation() {
        val rotationMatrix = FloatArray(9)
        if (lastAccelerometerReading != null && lastMagnetometerReading != null) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometerReading,
                lastMagnetometerReading
            )
            val orientation = FloatArray(ORIENTATION_MATRIX_SIZE)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            collectAzimuth(Azimuth(azimuth))
        }
    }

    companion object {
        const val TAG = "Location"
        const val ORIENTATION_MATRIX_SIZE = 3
        const val MINIMUM_ACCELERATION_DELTA = 0.5f
    }
}

