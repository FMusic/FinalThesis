package fm.pathfinder.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import fm.pathfinder.database.ApiData
import fm.pathfinder.database.ApiDataSingle
import fm.pathfinder.database.ApiHelper
import fm.pathfinder.filter.Kalman
import fm.pathfinder.filter.Kalman3d
import fm.pathfinder.model.Acceleration
import fm.pathfinder.utils.LimitedSizeQueue
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

    private val accelerationApi = ApiHelper("/accelerationvalues")
    private var rawDataApi = ApiData(mutableListOf())
    private var filteredDataApi = ApiData(mutableListOf())
    private var filtered3dDataApi = ApiData(mutableListOf())
    private val API_DATA_SIZE = 100

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

    private fun collectAcceleration(values: FloatArray?, timestamp: Long) {
        if (values == null) {
            return
        }
        sensorCollector.collectAccelerometer(values)
//        sensorCollector.collectAcceleration(newSensorValues(x, y, z))
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
            val filteredVlaue3d = kalman3d.update(filteredDataArray)
            if (collectApiData(filtered3dDataApi, filteredVlaue3d, timestamp) == 1) {
                filtered3dDataApi = ApiData(mutableListOf())
            }
        }
    }

    private suspend fun collectApiData(apiData: ApiData, vals: FloatArray, timestamp: Long): Int {
        apiData.data.add(ApiDataSingle(vals[0], vals[1], vals[2], timestamp))
        if (apiData.data.size >= API_DATA_SIZE) {
            accelerationApi.saveData(apiData)
            return 1
        }
        return 0
    }


    private fun newSensorValues(x: Float, y: Float, z: Float): Acceleration {
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

            CALIBRATION_QUEUE_SIZE - 1 -> {
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
        return Acceleration(0f, 0f, 0f)
    }

    private var lowerThreshold = 0.0

    private fun acceleration(x: Float, y: Float, z: Float): Acceleration {
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
            return Acceleration(0f, 0f, 0f)
        }
        Log.d("Acceleration", "Calibrated Values: X: $calibratedX Y: $calibratedY Z: $calibratedZ")
        return calibratedAcceleration
    }

    companion object {
        private const val CALIBRATION_QUEUE_SIZE = 1000
    }

}
