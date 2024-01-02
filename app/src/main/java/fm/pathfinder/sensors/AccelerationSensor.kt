package fm.pathfinder.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import fm.pathfinder.model.Acceleration
import fm.pathfinder.utils.LimitedSizeQueue
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class AccelerationSensor(
    val context: Context,
    private val sensorCollector: SensorCollector
) : SensorEventListener {
    private val calibrationQueueX = LimitedSizeQueue<Float>(CALIBRATION_QUEUE_SIZE)
    private val calibrationQueueY = LimitedSizeQueue<Float>(CALIBRATION_QUEUE_SIZE)
    private val calibrationQueueZ = LimitedSizeQueue<Float>(CALIBRATION_QUEUE_SIZE)

    init {
        val mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        val sensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP)
        mSensorManager.registerListener(this, sensor, sensorCollector.samplingPeriod)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We'll disregard this for now
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                notifyNewAccelerometerReading(event.values)
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                registerAcceleration(x, y, z)
            }
        }
    }

    private fun notifyNewAccelerometerReading(values: FloatArray?) {
        sensorCollector.collectAccelerometer(values)
    }

    private fun registerAcceleration(x: Float, y: Float, z: Float) {
        val acceleration = newSensorValues(x, y, z)
        if (acceleration != null) {
            notifyNewAcceleration(acceleration)
        }
    }

    private fun notifyNewAcceleration(acceleration: Acceleration) {
        sensorCollector.collectAcceleration(acceleration)
    }

    private fun newSensorValues(x: Float, y: Float, z: Float): Acceleration? {
        Log.d("Acceleration", "Raw Values: X: $x Y: $y Z: $z")
        Log.d(
            "Acceleration",
            "Calibration values: " +
                    "X: ${calibrationQueueX.average()} " +
                    "Y: ${calibrationQueueY.average()} " +
                    "Z: ${calibrationQueueZ.average()}"
        )
        if (calibrationQueueX.isFull()) {
            return acceleration(x, y, z)
        }
        when (calibrationQueueX.size) {
            0 -> {
                Toast.makeText(context, "Still Calibration Started", Toast.LENGTH_SHORT).show()
            }

            CALIBRATION_QUEUE_SIZE - 1 -> {
                Toast.makeText(context, "Still Calibration Finished", Toast.LENGTH_SHORT).show()
                Log.i(
                    "Acceleration",
                    "Calibration Values: " +
                            "X: ${calibrationQueueX.average()} " +
                            "Y: ${calibrationQueueY.average()} " +
                            "Z: ${calibrationQueueZ.average()}"
                )
                Log.i(
                    "Acceleration",
                    "Norm: ${
                        sqrt(
                            calibrationQueueX.average().pow(2) + calibrationQueueY.average()
                                .pow(2) + calibrationQueueZ.average().pow(2)
                        )
                    }"
                )
            }
        }
        calibrationQueueX.add(x.absoluteValue)
        calibrationQueueY.add(y.absoluteValue)
        calibrationQueueZ.add(z.absoluteValue)
        return null
    }

    private var lowerThreshold = 0.0

    private fun acceleration(x: Float, y: Float, z: Float): Acceleration? {
        val calibratedX =
            x.absoluteValue - calibrationQueueX.average().absoluteValue
        val calibratedY =
            y.absoluteValue - calibrationQueueY.average().absoluteValue
        val calibratedZ =
            z.absoluteValue - calibrationQueueZ.average().absoluteValue
        val calibratedAcceleration =
            Acceleration(calibratedX.toFloat(), calibratedY.toFloat(), calibratedZ.toFloat())
        if (calibratedAcceleration.norm() < lowerThreshold) {
            Log.e("Acceleration", "Acceleration is lower than the threshold, object is not moving")
            return null
        }
        Log.d("Acceleration", "Calibrated Values: X: $calibratedX Y: $calibratedY Z: $calibratedZ")
        return calibratedAcceleration
    }

    companion object {
        private const val CALIBRATION_QUEUE_SIZE = 1000
    }

}
