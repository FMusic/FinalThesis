package fm.pathfinder.collecting

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.opengl.Matrix.multiplyMV
import android.util.Log
import java.util.*
import java.util.stream.Collectors
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class VelocityProcessor(
    context: Context?,
    val collector: (Double) -> Unit,
    velocityOrientationMatrix: () -> FloatArray
) : SensorEventListener {
    private var velocity: Float = 0F
    private var appliedAcceleration: Float = 0F
    private var lastUpdatedate = Date(System.currentTimeMillis())
    private var currentAcceleration: Float = 0F
    private var calibration = Double.NaN
    private var accelerationByAxis = FloatArray(4)
    private var linearAccelerationVals = FloatArray(4)
    private var rotationMatrixInverse = FloatArray(16)

    private var lastTick = 0L

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

    class LimitedSizeQueue<K>(private val maxSize: Int) : ArrayList<K>() {
        override fun add(element: K): Boolean {
            val r = super.add(element)
            if (size > maxSize) {
                removeRange(0, size - maxSize)
            }
            return r
        }

        val isFull = size >= maxSize
    }

    private val NUMBER_OF_NEC_VALUES = 5
    private val eventValuesX = LimitedSizeQueue<Float>(NUMBER_OF_NEC_VALUES)
    private val eventValuesY = LimitedSizeQueue<Float>(NUMBER_OF_NEC_VALUES)
    private val eventValuesZ = LimitedSizeQueue<Float>(NUMBER_OF_NEC_VALUES)


    private fun pokusaj2(eventValues: FloatArray) {
        val tick = System.currentTimeMillis()
        val localPeriod = (tick - lastTick).toDouble().div(1000)

        lastTick = tick
        eventValuesX.add(eventValues[0])
        eventValuesY.add(eventValues[1])
        eventValuesZ.add(eventValues[2])
        if (!eventValuesX.isEmpty()) {
            val avgX =
                eventValuesX.stream().collect(Collectors.averagingDouble { it.toDouble() })
            val avgY =
                eventValuesY.stream().collect(Collectors.averagingDouble { it.toDouble() })
            val avgZ =
                eventValuesZ.stream().collect(Collectors.averagingDouble { it.toDouble() })
            val xSign = avgX > 0
            val ySign = avgY > 0
            val motion = sqrt(avgX.pow(2) + avgY.pow(2))
            if (motion > 0.1) {
                val speedInM = motion.times(localPeriod)
                val distanceInM = speedInM.times(localPeriod)
                Log.i( "Velocity", "Motion: $motion, " +
                       "Period: $localPeriod, " +
                       "Speed: $speedInM, " +
                       "Distance: $distanceInM" )
                if (distanceInM < 20) {
                    collector(distanceInM)
                }
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
