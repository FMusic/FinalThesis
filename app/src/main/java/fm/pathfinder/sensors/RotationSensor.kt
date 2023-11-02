package fm.pathfinder.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import fm.pathfinder.model.Azimuth

class RotationSensor(
    private val context: Context?,
    private val collector: (Float) -> Unit
) : SensorEventListener {
    private val velocityOrientationMatrixInverted = FloatArray(16)
    private val rotationMatrix = FloatArray(9)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                calculateAzimuth(event.values)
            }
        }
    }

    private fun calculateAzimuth(values: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        val remappedRotationMatrix = when (context?.display?.rotation) {
            Surface.ROTATION_90 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            Surface.ROTATION_180 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            Surface.ROTATION_270 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
            else -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
        }
        val orientationInRadians = SensorManager.getOrientation(remappedRotationMatrix, FloatArray(3))
        val azimuth = Azimuth(Math.toDegrees(orientationInRadians[0].toDouble()).toFloat())
//        Log.d("Orientation", "Angle: $azimuth")
        collector(azimuth.degrees)
    }

    private fun remapRotationMatrix(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val remappedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, remappedRotationMatrix)
        return remappedRotationMatrix
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun getVelocityOrientationMatrix(): FloatArray {
        return velocityOrientationMatrixInverted
    }

}
