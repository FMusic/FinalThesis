package fm.pathfinder.collecting

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.opengl.Matrix.multiplyMV
import android.util.Log
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class VelocityProcessor(context: Context?, val collector: (Float) -> Unit) : SensorEventListener {
    private var velocity: Float = 0F
    private var appliedAcceleration: Float = 0F
    private var lastUpdatedate = Date(System.currentTimeMillis())
    private var currentAcceleration: Float = 0F
    private var calibration = Double.NaN
    private var accelerationByAxis = FloatArray(4)
    private var linearAccelerationVals = FloatArray(4)
    private var rotationMatrixInverse = FloatArray(16)

    private var lastTick = System.currentTimeMillis()
    private val samplePeriod = 15

    private val DISTANCE_CALI = 20

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
//                nes(event.values)
                pokusaj2(event.values)
            }
            Sensor.TYPE_ACCELEROMETER -> {
//                calculateVelocity(event.values)
            }
        }
    }

    private fun pokusaj2(eventValues: FloatArray) {
        val tick = System.currentTimeMillis()
        val localPeriod = tick - lastTick

        if (localPeriod > samplePeriod) {
            lastTick = tick
            val motion = sqrt(
                eventValues[0].pow(2)
                        + eventValues[1].pow(2)
//                        + eventValues[2].pow(2)
            )
            val distanceInMM = motion.times(localPeriod)
            if (distanceInMM > DISTANCE_CALI){
                collector(distanceInMM.div(1000))
            }

        }
    }

    private fun nes(eventValues: FloatArray) {

        Log.i("Acc", "AccelDE: ${eventValues[0]} ${eventValues[1]} ${eventValues[2]}")
        System.arraycopy(eventValues, 0, linearAccelerationVals, 0, eventValues.size)
        multiplyMV(accelerationByAxis, 0, rotationMatrixInverse, 0, linearAccelerationVals, 0)
        val lastAbsoluteAccelerationString =
            "${accelerationByAxis[0]} ${accelerationByAxis[1]} ${accelerationByAxis[2]}"
        Log.i("Acc", "Acceleration: $lastAbsoluteAccelerationString")
    }

    private fun calculateVelocity(eventValues: FloatArray?) {
        eventValues?.let {
            val x = eventValues[0]
            val y = eventValues[0]
            val z = eventValues[0]
            val a = sqrt(x.pow(2) + y.pow(2) + z.pow(2)).toDouble()
            if (calibration.isNaN()) {
                calibration = a
            } else {
                updateVelocity()
                currentAcceleration = a.toFloat()
            }
        }
    }

    private fun updateVelocity() {
        // Calculate how long this acceleration has been applied.

        // Calculate how long this acceleration has been applied.
        val timeNow = Date(System.currentTimeMillis())
        val timeDelta: Long = timeNow.time - lastUpdatedate.time
        lastUpdatedate.time = timeNow.time

        // Calculate the change in velocity at the
        // current acceleration since the last update.

        // Calculate the change in velocity at the
        // current acceleration since the last update.
        val deltaVelocity: Float = appliedAcceleration * (timeDelta / 1000)
        appliedAcceleration = currentAcceleration

        // Add the velocity change to the current velocity.

        // Add the velocity change to the current velocity.
        velocity += deltaVelocity

        val mph = ((100 * velocity / 1.6 * 3.6).roundToInt() / 100).toDouble()
        Log.i("SensorTestActivity", "SPEEDDDDD=== $mph $velocity")

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}
