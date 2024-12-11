package fm.pathfinder.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import fm.pathfinder.model.Azimuth
import kotlin.math.abs

class RotationSensor(
    private val context: Context,
    private val sensorCollector: SensorCollector
) : SensorEventListener {
    private val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
    private var lastTimestamp: Long = 0
    private var lastAzimuth: Float = 0f

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO: We'll disregard this for now
    }

    init {
        val mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        mSensorManager.registerListener(this, sensor, sensorCollector.samplingPeriod)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                lastTimestamp = event.timestamp
                notifyNewMagenetometer(event.values)

//                val azimuth = calculateAzimuth(event.values)
//                notifyNewAzimuth(azimuth.degrees)
            }
        }
    }

    private fun notifyNewMagenetometer(values: FloatArray) {
        sensorCollector.collectMagnetometer(values)
    }

    private fun notifyNewAzimuth(degrees: Float) {
        val delta = degrees - lastAzimuth
        if (abs(delta) > MINIMUM_AZIMUTH_DELTA) {
            lastAzimuth = degrees
            sensorCollector.collectAzimuth(Azimuth(degrees))
        }
    }

    private fun calculateAzimuth(values: FloatArray): Azimuth {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        val remappedRotationMatrix = when (context.display?.rotation) {
            Surface.ROTATION_90 -> remapRotationMatrix(
                rotationMatrix,
                SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_X
            )

            Surface.ROTATION_180 -> remapRotationMatrix(
                rotationMatrix,
                SensorManager.AXIS_MINUS_X,
                SensorManager.AXIS_MINUS_Y
            )

            Surface.ROTATION_270 -> remapRotationMatrix(
                rotationMatrix,
                SensorManager.AXIS_MINUS_Y,
                SensorManager.AXIS_X
            )

            else -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
        }
        val orientationInRadians =
            SensorManager.getOrientation(
                remappedRotationMatrix,
                FloatArray(ORIENTATION_MATRIX_SIZE)
            )
        return Azimuth(Math.toDegrees(orientationInRadians[0].toDouble()).toFloat())

    }

    private fun remapRotationMatrix(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val remappedRotationMatrix = FloatArray(REMAPPED_MATRIX_SIZE)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, remappedRotationMatrix)
        return remappedRotationMatrix
    }

    companion object {
        const val TAG = "Rotation"
        const val REMAPPED_MATRIX_SIZE = 9
        const val ROTATION_MATRIX_SIZE = 9
        const val ORIENTATION_MATRIX_SIZE = 3
        const val MINIMUM_AZIMUTH_DELTA = 5.0f
    }


}
