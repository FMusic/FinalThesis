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
import fm.pathfinder.utils.LimitedSizeQueue
import kotlin.math.atan2

class OrientationProcessor(
    private val context: Context?,
    private val collector: (Float) -> Unit
) : SensorEventListener {
    private val velocityOrientationMatrixInverted = FloatArray(16)
    private val rotationMatrix = FloatArray(9)
    private val velocityOrientationMatrix = FloatArray(16)

    private val lastAngles = LimitedSizeQueue<Float>(10)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                SensorManager.getRotationMatrixFromVector(velocityOrientationMatrix, event.values)
                invertM(velocityOrientationMatrixInverted, 0, velocityOrientationMatrix, 0)
                calculateAngle2(event.values)

            }
        }
    }

    private fun calculateAngle(eventValues: FloatArray) {
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
        lastAngles.add(angle)
        val anglesAvg = lastAngles.average()
        val finalAngle = round(anglesAvg.toFloat())

        Log.d("Orientation", "Angle: $finalAngle, Average: $anglesAvg")
        collector(finalAngle)
    }


    private fun calculateAngle2(eventValues: FloatArray){
        val (worldAxisForDeviceAxisX, worldAxisForDeviceAxisY) = when (context?.display?.rotation) {
            Surface.ROTATION_0 -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Z)
            Surface.ROTATION_90 -> Pair(SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X)
            Surface.ROTATION_180 -> Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Z)
            Surface.ROTATION_270 -> Pair(SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X)
            else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Z)
        }

        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
            worldAxisForDeviceAxisY, adjustedRotationMatrix)

        // Transform rotation matrix into azimuth/pitch/roll
        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)

        // Convert radians to degrees
        val azimuth = orientation[0] * -57
        val pitch = orientation[1] * -57
        val roll = orientation[2] * -57

        val humanAzimuth = formatAndroidAngleToHumanAngle(azimuth)

        lastAngles.add(humanAzimuth)
        val anglesAvg = lastAngles.average()
        val finalAngle = round(anglesAvg.toFloat())

        Log.d("Orientation", "Angle: $finalAngle, Average: $anglesAvg")
        collector(finalAngle)
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
        val accAngle = angle
//            .times(1.125F)
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
