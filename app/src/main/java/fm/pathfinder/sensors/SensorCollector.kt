package fm.pathfinder.sensors

import android.content.Context
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
    private var lastAzimuth: Float = 0f
    private lateinit var wifiSensor: WifiSensor
    private lateinit var gpsProcessor: GpsSensor
    private lateinit var rotationSensor: RotationSensor
    private lateinit var accelerationSensor: AccelerationSensor
    private var scanningOn = false
    val samplingPeriod = SensorManager.SENSOR_DELAY_UI
    private var lastAcceleration: Acceleration? = null
    private var timestampBegin: Long = 0
    private var maxAcceleration = Acceleration(0f, 0f, 0f)

    private fun initSensors() {
//        gpsProcessor = GpsSensor(context, building)
        rotationSensor = RotationSensor(context, this)
        accelerationSensor = AccelerationSensor(context, this)
        wifiSensor = WifiSensor(context, this)
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
        if (scanningOn && acceleration.norm() > 0.5f) {
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
                    if(stepPauseCounter == 1){
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
        val direction = lastAzimuth
        val distance = speed * time
        maxAcceleration = Acceleration(0f, 0f, 0f)
        Log.i(TAG, "Step: $distance, $direction")
        mapPresenter.newStep(distance)
    }

    companion object {
        const val TAG = "Location"
    }
}

