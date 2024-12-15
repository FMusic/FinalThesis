package fm.pathfinder.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import fm.pathfinder.database.ApiData
import fm.pathfinder.database.ApiDataSingle
import fm.pathfinder.database.ApiHelper
import fm.pathfinder.filter.Kalman
import fm.pathfinder.filter.Kalman3d
import fm.pathfinder.model.Azimuth
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

    private val orientationApi = ApiHelper("/orientationvalues")
    private var rawDataApi = ApiData(mutableListOf())
    private var filteredDataApi = ApiData(mutableListOf())
    private var filtered3dDataApi = ApiData(mutableListOf())
    private val API_DATA_SIZE = 100

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
        if (values == null) {
            return
        }
        sensorCollector.collectMagnetometer(values)
        sensorCollector.collectAzimuth(Azimuth(values[0]))
        CoroutineScope(Dispatchers.Default).launch {
            if (collectApiData(rawDataApi, values, timestamp) == 1) {
                rawDataApi = ApiData(mutableListOf())
            }
            val filteredDataArray = floatArrayOf(
                kalmanX.measure(values[0]),
                kalmanY.measure(values[1]),
                kalmanZ.measure(values[2])
            )
            if (collectApiData(filteredDataApi, filteredDataArray, timestamp) == 1) {
                filteredDataApi = ApiData(mutableListOf())
            }
            val filteredValue3d = kalman3d.update(filteredDataArray)
            if (collectApiData(filtered3dDataApi, filteredValue3d, timestamp) == 1) {
                filtered3dDataApi = ApiData(mutableListOf())
            }
        }
    }

    private suspend fun collectApiData(apiData: ApiData, values: FloatArray, timestamp: Long): Int {
        apiData.data.add(ApiDataSingle(values[0], values[1], values[2], timestamp))
        if (apiData.data.size >= API_DATA_SIZE) {
            orientationApi.saveData(apiData)
            return 1
        }
        return 0
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
