package fm.pathfinder.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import fm.pathfinder.filter.Kalman
import fm.pathfinder.filter.Kalman3d
import fm.pathfinder.model.Acceleration
import fm.pathfinder.utils.API_ENDPOINTS
import fm.pathfinder.utils.ApiData
import fm.pathfinder.utils.ApiHelper
import fm.pathfinder.utils.LimitedSizeQueue
import fm.pathfinder.utils.VectorOperations.normalizeVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class AccelerationSensor(
    private val sensorCollector: SensorCollector
) : SensorEventListener {
    private val calibrationQueueX = LimitedSizeQueue<Float>(CALIBRATION_QUEUE_SIZE)
    private val calibrationQueueY = LimitedSizeQueue<Float>(CALIBRATION_QUEUE_SIZE)
    private val calibrationQueueZ = LimitedSizeQueue<Float>(CALIBRATION_QUEUE_SIZE)

    private val kalmanX = Kalman()
    private val kalmanY = Kalman()
    private val kalmanZ = Kalman()
    private val kalman3d = Kalman3d(0.1f, 0.1f, floatArrayOf(0f, 0f, 0f))

    private var beginTime = 0L

    private val accelerationApi = ApiHelper()
    private var rawDataApi = ApiData(mutableListOf())
    private var filteredDataApi = ApiData(mutableListOf())
    private var filtered3dDataApi = ApiData(mutableListOf())
    private val API_DATA_SIZE = 500

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // We'll disregard this for now
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                collectAcceleration(event.values, event.timestamp)
            }
        }
    }

    /**
     * Collects acceleration data from the sensor and filters it using a Kalman filter. The filtered
     * data is then sent to the API.
     * @param values: The acceleration values from the sensor
     * @param timestamp: The timestamp of the sensor event
     * @return void
     * @see SensorEvent
     * @see Sensor
     * @see Kalman
     */
    private fun collectAcceleration(values: FloatArray?, timestamp: Long) {
        beginTime = if (beginTime != 0L) beginTime else timestamp
        val ts = timestamp - beginTime
        if (values == null) {
            return
        }
        sensorCollector.collectAccelerometer(values)
        Log.i("Acceleration", "Raw Values: X: ${values[0]} Y: ${values[1]} Z: ${values[2]}")
//        sensorCollector.collectAcceleration(newSensorValues(x, y, z))
        CoroutineScope(Dispatchers.Default).launch {
            val rawData = floatArrayOf(values[0], values[1], values[2], normalizeVector(values))
            val resRaw = collectApiData(rawData, ts, DATA_TYPE.RAW)
            if (resRaw == 1) {
                Log.i("Acceleration", "Raw Data sent to API")
                rawDataApi = ApiData(mutableListOf())
            }
            val predictedKalmanData = floatArrayOf(
                kalmanX.P, kalmanY.P, kalmanZ.P
            )
            val filteredKalmanData = floatArrayOf(
                kalmanX.update(values[0]), kalmanY.update(values[1]), kalmanZ.update(values[2])
            )
            val resFilter = collectApiData(
                floatArrayOf(
                    predictedKalmanData[0], predictedKalmanData[1], predictedKalmanData[2],
                    filteredKalmanData[0], filteredKalmanData[1], filteredKalmanData[2],
                    normalizeVector(predictedKalmanData), normalizeVector(filteredKalmanData),
                ), ts, DATA_TYPE.FILTERED
            )
            if (resFilter == 1) {
                Log.i("Acceleration", "Filtered Data sent to API")
                filteredDataApi = ApiData(mutableListOf())
            }
            val predictedKalmanData3d = floatArrayOf(
                kalman3d.P[0], kalman3d.P[1], kalman3d.P[2]
            )
            val filteredVlaue3d = kalman3d.update(rawData)
            val res3d = collectApiData(
                floatArrayOf(
                    predictedKalmanData3d[0],
                    predictedKalmanData3d[1],
                    predictedKalmanData3d[2],
                    filteredVlaue3d[0],
                    filteredVlaue3d[1],
                    filteredVlaue3d[2],
                    normalizeVector(predictedKalmanData3d),
                    normalizeVector(filteredVlaue3d)
                ), ts, DATA_TYPE.FILTERED_3D
            )
            if (res3d == 1) {
                Log.i("Acceleration", "3d Data sent to API")
                filtered3dDataApi = ApiData(mutableListOf())
            }
        }
    }

    /**
     * Collects the filtered acceleration data and if queue is full, sends it to the API.
     * @param apiData: The API data object
     * @param vals: The acceleration values
     * @param ts: The timestamp of the sensor event
     * @param apiHelper: The API helper object
     * @return Int
     * @see ApiData
     * @see ApiDataSingle
     * @see ApiHelper
     *
     */
    private suspend fun collectApiData(
        vals: FloatArray?,
        ts: Long,
        dataType: DATA_TYPE,
    ): Int {
        if (vals == null) {
            sendDataToApi(dataType)
            return 1
        }
        when (dataType) {
            DATA_TYPE.RAW -> {
                rawDataApi.data.add(
                    mapOf(
                        "x" to vals[0],
                        "y" to vals[1],
                        "z" to vals[2],
                        "normalization" to vals[3],
                        "timestamp" to ts
                    )
                )
                if (rawDataApi.data.size >= API_DATA_SIZE) {
                    return 1
                }
            }

            DATA_TYPE.FILTERED -> {
                filteredDataApi.data.add(
                    mapOf(
                        "predictionx" to vals[0],
                        "predictiony" to vals[1],
                        "predictionz" to vals[2],
                        "filteredx" to vals[3],
                        "filteredy" to vals[4],
                        "filteredz" to vals[5],
                        "normalizedprediction" to vals[6],
                        "normalizedfiltered" to vals[7],
                        "timestamp" to ts
                    )
                )
                if (filteredDataApi.data.size >= API_DATA_SIZE) {
                    return 1
                }
            }

            DATA_TYPE.FILTERED_3D -> {
                filtered3dDataApi.data.add(
                    mapOf(
                        "predictionx" to vals[0],
                        "predictiony" to vals[1],
                        "predictionz" to vals[2],
                        "filteredx" to vals[3],
                        "filteredy" to vals[4],
                        "filteredz" to vals[5],
                        "normalizedprediction" to vals[6],
                        "normalizedfiltered" to vals[7],
                        "timestamp" to ts
                    )
                )
                if (filtered3dDataApi.data.size >= API_DATA_SIZE) {
                    return 1
                }
            }
        }
        return 0
    }

    private suspend fun sendDataToApi(dataType: DATA_TYPE) {
        when (dataType) {
            DATA_TYPE.RAW -> {
                accelerationApi.saveData(rawDataApi.copy(), API_ENDPOINTS.ACCELERATION_VALUES)
                rawDataApi = ApiData(mutableListOf())
            }
            DATA_TYPE.FILTERED -> {
                accelerationApi.saveData(filteredDataApi.copy(), API_ENDPOINTS.ACCELERATION_FILTERED)
                filteredDataApi = ApiData(mutableListOf())
            }
            DATA_TYPE.FILTERED_3D -> {
                accelerationApi.saveData(filtered3dDataApi.copy(), API_ENDPOINTS.ACCELERATION_FILTERED_3D)
                filtered3dDataApi = ApiData(mutableListOf())
            }
        }
    }

    enum class DATA_TYPE {
        RAW, FILTERED, FILTERED_3D
    }


    private fun newSensorValues(x: Float, y: Float, z: Float): Acceleration {
        Log.d("Acceleration", "Raw Values: X: $x Y: $y Z: $z")
        Log.d(
            "Acceleration",
            "Calibration values: " + "X: ${calibrationQueueX.average()} " + "Y: ${calibrationQueueY.average()} " + "Z: ${calibrationQueueZ.average()}"
        )
        if (calibrationQueueX.isFull()) {
            return acceleration(x, y, z)
        }
        when (calibrationQueueX.size) {

            CALIBRATION_QUEUE_SIZE - 1 -> {
                Log.i(
                    "Acceleration",
                    "Calibration Values: " + "X: ${calibrationQueueX.average()} " + "Y: ${calibrationQueueY.average()} " + "Z: ${calibrationQueueZ.average()}"
                )
                Log.i(
                    "Acceleration", "Norm: ${
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
        return Acceleration(0f, 0f, 0f)
    }

    private var lowerThreshold = 0.0

    private fun acceleration(x: Float, y: Float, z: Float): Acceleration {
        val calibratedX = x.absoluteValue - calibrationQueueX.average().absoluteValue
        val calibratedY = y.absoluteValue - calibrationQueueY.average().absoluteValue
        val calibratedZ = z.absoluteValue - calibrationQueueZ.average().absoluteValue
        val calibratedAcceleration =
            Acceleration(calibratedX.toFloat(), calibratedY.toFloat(), calibratedZ.toFloat())
        if (calibratedAcceleration.norm() < lowerThreshold) {
            Log.e("Acceleration", "Acceleration is lower than the threshold, object is not moving")
            return Acceleration(0f, 0f, 0f)
        }
        Log.d("Acceleration", "Calibrated Values: X: $calibratedX Y: $calibratedY Z: $calibratedZ")
        return calibratedAcceleration
    }

    suspend fun unregister() {
        sendDataToApi(DATA_TYPE.RAW)
        sendDataToApi(DATA_TYPE.FILTERED)
        sendDataToApi(DATA_TYPE.FILTERED_3D)
    }

    companion object {
        private const val CALIBRATION_QUEUE_SIZE = 1000
    }

}
