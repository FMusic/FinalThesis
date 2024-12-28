package fm.pathfinder.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import fm.pathfinder.filter.Kalman
import fm.pathfinder.filter.Kalman3d
import fm.pathfinder.model.Azimuth
import fm.pathfinder.utils.API_ENDPOINTS
import fm.pathfinder.utils.ApiData
import fm.pathfinder.utils.ApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RotationSensor(
    private val sensorCollector: SensorCollector
) : SensorEventListener {
    private val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
    private var lastTimestamp: Long = 0
    private var lastAzimuth: Float = 0f

    private val kalmanX = Kalman()
    private val kalmanY = Kalman()
    private val kalmanZ = Kalman()
    private val kalman3d = Kalman3d(0.1f, 0.1f, floatArrayOf(0f, 0f, 0f))

    private var beginTime = 0L

    private val orientationApi = ApiHelper()
    private var rawDataApi = ApiData(mutableListOf())
    private val API_DATA_SIZE = 300

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO: We'll disregard this for now
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                collectOrientation(event.values, event.timestamp)
            }
        }
    }

    private fun collectOrientation(values: FloatArray?, timestamp: Long) {
        beginTime = if (beginTime != 0L) beginTime else timestamp
        val ts = (timestamp - beginTime) / 1_000_000
        if (values == null) {
            return
        }
        sensorCollector.collectMagnetometer(values)
        sensorCollector.collectAzimuth(Azimuth(values[0]))
        CoroutineScope(Dispatchers.Default).launch {
            collectApiData(
                floatArrayOf(
                    values[0],
                    values[1],
                    values[2],
                    values[3],
                    getAzimuth(values)
                ), ts
            )
        }
    }

    private fun getAzimuth(values: FloatArray): Float {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)

        // Adjust rotation matrix based on device orientation if necessary
        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Y,
            adjustedRotationMatrix
        )

        val orientation = FloatArray(3)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)

        // Convert azimuth from radians to degrees
        val azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
        Log.d("Azimuth", "Azimuth in degrees: $azimuthDegrees")
        return azimuthDegrees
    }

    private suspend fun collectApiData(
        values: FloatArray?, timestamp: Long?
    ): Int {
        if (values == null || timestamp == null) {
            sendDataToApi()
            return 1
        }
        rawDataApi.data.add(
            mapOf(
                "x" to values[0],
                "y" to values[1],
                "z" to values[2],
                "w" to values[3],
                "azimuth" to values[4],
                "timestamp" to timestamp
            )
        );
        if (rawDataApi.data.size >= API_DATA_SIZE) {
            sendDataToApi()
            return 1
        }
        return 0;
    }

    private suspend fun sendDataToApi() {
        orientationApi.saveData(rawDataApi.copy(), API_ENDPOINTS.ORIENTATION_VALUES)
        rawDataApi = ApiData(mutableListOf())
    }

    suspend fun unregister() {
        sendDataToApi()
    }

//    private fun notifyNewAzimuth(degrees: Float) {
//        val delta = degrees - lastAzimuth
//        if (abs(delta) > MINIMUM_AZIMUTH_DELTA) {
//            lastAzimuth = degrees
//            sensorCollector.collectAzimuth(Azimuth(degrees))
//        }
//    }
//
//    private fun calculateAzimuth(values: FloatArray): Azimuth {
//        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
//        val remappedRotationMatrix = when (context.display?.rotation) {
//            Surface.ROTATION_90 -> remapRotationMatrix(
//                rotationMatrix,
//                SensorManager.AXIS_Y,
//                SensorManager.AXIS_MINUS_X
//            )
//
//            Surface.ROTATION_180 -> remapRotationMatrix(
//                rotationMatrix,
//                SensorManager.AXIS_MINUS_X,
//                SensorManager.AXIS_MINUS_Y
//            )
//
//            Surface.ROTATION_270 -> remapRotationMatrix(
//                rotationMatrix,
//                SensorManager.AXIS_MINUS_Y,
//                SensorManager.AXIS_X
//            )
//
//            else -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
//        }
//        val orientationInRadians =
//            SensorManager.getOrientation(
//                remappedRotationMatrix,
//                FloatArray(ORIENTATION_MATRIX_SIZE)
//            )
//        return Azimuth(Math.toDegrees(orientationInRadians[0].toDouble()).toFloat())
//
//    }
//
//    private fun remapRotationMatrix(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
//        val remappedRotationMatrix = FloatArray(REMAPPED_MATRIX_SIZE)
//        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, remappedRotationMatrix)
//        return remappedRotationMatrix
//    }

    companion object {
        const val TAG = "Rotation"
        const val REMAPPED_MATRIX_SIZE = 9
        const val ROTATION_MATRIX_SIZE = 9
        const val ORIENTATION_MATRIX_SIZE = 3
        const val MINIMUM_AZIMUTH_DELTA = 5.0f
    }


}
