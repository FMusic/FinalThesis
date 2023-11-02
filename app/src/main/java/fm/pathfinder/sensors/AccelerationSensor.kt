package fm.pathfinder.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import fm.pathfinder.utils.LimitedSizeQueue
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class AccelerationSensor(
    context: Context?,
    val collector: (Double) -> Unit,
) : SensorEventListener {
    private var velocity: Float = 0F
    private var appliedAcceleration: Float = 0F
    private var lastUpdatedate = Date(System.currentTimeMillis())
    private var currentAcceleration: Float = 0F
    private var calibration = Double.NaN
    private var accelerationByAxis = FloatArray(4)
    private var linearAccelerationVals = FloatArray(4)
    private var rotationMatrixInverse = FloatArray(16)

    private var lastSpeed = 0.0

    private var lastTick = 0L

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
            }
        }
    }

}
