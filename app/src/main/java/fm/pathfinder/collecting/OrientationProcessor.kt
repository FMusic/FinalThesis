package fm.pathfinder.collecting

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.Matrix.invertM
import android.util.Log
import android.view.Surface
import fm.pathfinder.exceptions.ProcessingException
import kotlin.math.atan2

class OrientationProcessor(
    private val context: Context?,
    private val collector: (Float) -> Unit
) : SensorEventListener {
    private val velocityOrientationMatrixInverted = FloatArray(16)
    private val rotationMatrix = FloatArray(9)
    private val velocityOrientationMatrix = FloatArray(16)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getRotationMatrixFromVector(velocityOrientationMatrix, event.values)
                invertM(velocityOrientationMatrixInverted, 0, velocityOrientationMatrix, 0)
                calculateAngle(event.values, rotationMatrix)
            }
        }
    }

    private fun calculateAngle(eventValues: FloatArray, rotationMatrix: FloatArray) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, eventValues)
        val (matrixColumn, sense) = when (val rotation = context?.display?.rotation) {
            Surface.ROTATION_0 -> Pair(0, 1)
            Surface.ROTATION_90 -> Pair(1, -1)
            Surface.ROTATION_180 -> Pair(0, -1)
            Surface.ROTATION_270 -> Pair(1, 1)
            else -> error("Invalid screen rotation value: $rotation")
        }
        val x = sense * rotationMatrix[matrixColumn]
        val y = sense * rotationMatrix[matrixColumn + 3]
        var angle = -atan2(y, x).times(-52)
        angle = formatAndroidAngleToHumanAngle(angle)
        collector(round(angle))
    }

    private fun round(angle: Float): Float {
        if (angle <= 45 || angle > 315) {
            return 0F
        } else if (angle > 45 && angle <= 135) {
            return 90F
        } else if (angle > 135 && angle <= 225) {
            return 180F
        } else if (angle > 225 && angle <= 315) {
            return 270F
        }
        throw ProcessingException("Degrees are not aligned")
    }

    private fun formatAndroidAngleToHumanAngle(angle: Float): Float {
        val accAngle = angle.times(1.125F)
        return if (accAngle < 0) {
            accAngle + 360F
        } else
            accAngle
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    public fun getVelocityOrientationMatrix(): FloatArray {
        return velocityOrientationMatrixInverted
    }

}
