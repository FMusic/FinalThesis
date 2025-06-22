package fm.pathfinder.sensor

import android.hardware.SensorManager.getRotationMatrixFromVector
import android.util.Log
import fm.pathfinder.model.ErrorState
import fm.pathfinder.model.SensorBias
import fm.pathfinder.utils.API_ENDPOINTS
import fm.pathfinder.utils.ApiHelper
import fm.pathfinder.utils.Extensions.norm
import fm.pathfinder.utils.MathUtils.matrixMultiply
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow

class SensorCollector(private val strideLength: Float) {
    private val kalmanFilter = AdaptiveKalmanFilter(strideLength)

    // For computing Δt.
    private var lastUpdateTime: Long = 0

    // Initialize the error state (all zeros initially) and sensor bias (calibrate as needed).
    private var errorState = ErrorState(
        positionError = floatArrayOf(0.01f, 0.01f, 0.01f),
        velocityError = floatArrayOf(0.01f, 0.01f, 0.01f),
        attitudeError = floatArrayOf(0.01f, 0.01f, 0.01f),
    )
    private val sensorBias = SensorBias(
        accelerometerBias = floatArrayOf(0.1f, 0.1f, 0.1f),
        gyroscopeBias = floatArrayOf(0.1f, 0.1f, 0.1f)
    )

    // For step position and velocity updates.
    private var previousStepPosition = floatArrayOf(0f, 0f, 0f)  // in navigation frame (meters)
    private var currentStepPosition = floatArrayOf(0f, 0f, 0f)

    // We'll compute step velocity as step length divided by the time between steps.
    private var lastStepTime: Long = 0

    // A sliding window for vertical acceleration (timestamp in ms, verticalAcc in m/s^2).
    private val verticalAccWindow = mutableListOf<Pair<Long, Float>>()

    // Threshold for detecting a step (tunable).
    private val verticalAccDiffThreshold = 2.0f  // e.g., 2 m/s^2 difference

    // Buffers to store sensor data.
    // The rotation vector is converted to a 3x3 rotation matrix.
    private var rotationMatrix: Array<FloatArray>? = null
    private var acceleration: FloatArray? = null

    private val apiHelper = ApiHelper()
    private var apiDataArray = mutableListOf<Map<String, Any>>() // Store data for API
    private var rawAccelerationData = mutableListOf<Map<String, Any>>()
    private var rawOrientationData = mutableListOf<Map<String, Any>>()
    private var filteredAccelerationData = mutableListOf<Map<String, Any>>()
    private var clickedStepData = mutableListOf<Map<String, Any>>()

    /**
     * In a real application you would use the rotation vector sensor
     * to obtain the device-to-navigation frame rotation matrix.
     */
    fun rotationValues(values: FloatArray, timestamp: Long) {
        Log.d("SensorCollector", "Rotation: ${values[0]}, ${values[1]}, ${values[2]}, ${values[3]}")
        val rotMat = FloatArray(9)
        getRotationMatrixFromVector(rotMat, values)
        // Convert the flat 9-element array into a 3x3 matrix.
        rotationMatrix = arrayOf(
            floatArrayOf(rotMat[0], rotMat[1], rotMat[2]),
            floatArrayOf(rotMat[3], rotMat[4], rotMat[5]),
            floatArrayOf(rotMat[6], rotMat[7], rotMat[8])
        )
        rawOrientationData.add(
            mapOf(
                "timestamp" to timestamp,
                "x" to values[0],
                "y" to values[1],
                "z" to values[2],
                "w" to values[3]
            )
        )
        if (rawOrientationData.size >= 10) {
            saveRawOrientationData()
        }
    }

    /**
     * Processes accelerometer values.
     *
     * In addition to simply storing the latest acceleration, we also compute the vertical acceleration
     * (in the navigation frame) and add it to a sliding window for step detection.
     */
    fun accelerationValues(values: FloatArray, timestamp: Long) {
        Log.d("SensorCollector", "Acceleration: ${values[0]}, ${values[1]}, ${values[2]}")
        acceleration = values.clone()

        // Compute vertical acceleration (assume the third row of rotationMatrix gives the vertical direction).
        // If rotationMatrix is not yet available, fallback to raw z-axis.
        val verticalAcc = if (rotationMatrix != null) {
            rotationMatrix!![2][0] * values[0] +
                    rotationMatrix!![2][1] * values[1] +
                    rotationMatrix!![2][2] * values[2]
        } else {
            values[2]
        }

        // Add the (timestamp, verticalAcc) pair to the window.
        verticalAccWindow.add(Pair(timestamp, verticalAcc))

        // Optionally, clear old samples if the window grows too large.
        if (verticalAccWindow.size > 50) {  // adjust window size as needed
            verticalAccWindow.removeAt(0)
        }
        acceleration = values.clone()
        calculateErrorState(timestamp)
        rawAccelerationData.add(
            mapOf(
                "timestamp" to timestamp,
                "x" to values[0],
                "y" to values[1],
                "z" to values[2],
                "normalization" to values.norm()
            )
        )
        if (rawAccelerationData.size >= 10) {
            saveRawAccelerationData()
        }

    }

    /**
     * This function is called when a new step button is clicked. Data will allow us to compare
     * step timestamp with algorithm calculated step timestamp.
     */
    fun newStep(timestamp: Long) {
        val apiData = mapOf(
            "timestamp" to timestamp
        )
        clickedStepData.add(apiData)
        if(clickedStepData.size >= 10) {
            CoroutineScope(Dispatchers.Default).launch {
                apiHelper.saveData(clickedStepData, API_ENDPOINTS.STEP_CLICKED)
                clickedStepData.clear()
            }
        }
    }

    /**
     * This function updates the error state using the Kalman filter.
     * It performs state propagation, ZUPT, and then checks for step detection.
     */
    private fun calculateErrorState(timestamp: Long) {
        // When both rotation and acceleration data are available, update the filter.
        if (rotationMatrix == null || acceleration == null) {
            Log.e("SensorCollector", "Rotation or acceleration data missing.")
            return
        }
        // Compute Δt in seconds.
        val deltaTime = (timestamp - lastUpdateTime).toFloat()
        lastUpdateTime = timestamp

        // Propagate the error state using the current acceleration measurement.
        errorState = kalmanFilter.propagateErrorState(
            errorState,
            sensorBias,
            acceleration!!,
            deltaTime,
            rotationMatrix!!
        )
        Log.i("SensorCollector", "Error state propagated, errorState = $errorState")

        val filteredAcceleration =
            FloatArray(3) { i -> acceleration!![i] - sensorBias.accelerometerBias[i] }

        filteredAccelerationData.add(
            mapOf(
                "timestamp" to timestamp,
                "x" to filteredAcceleration[0],
                "y" to filteredAcceleration[1],
                "z" to filteredAcceleration[2],
                "normalization" to filteredAcceleration.norm()
            )
        )
        if (filteredAccelerationData.size >= 10) {
            saveFilteredAccelerationData()
        }

        // Apply Zero Velocity Update (ZUPT) when the acceleration indicates near-zero motion.
        if (ZUPTFilter.detectZeroVelocity(acceleration!!)) {
            Log.i("SensorCollector", "Zero velocity detected.")
            // Here we expect the velocity to be zero.
            errorState = kalmanFilter.updateErrorStateWithZUPT(
                errorState,
                floatArrayOf(0f, 0f, 0f), // expecting zero velocity
                deltaTime
            )
        }

        // --- Step Detection Logic ---
        // When the vertical acceleration window has enough samples, check if a step occurred.
        if (verticalAccWindow.size < 10) {
            Log.e("SensorCollector", "Not enough samples for step detection.")
            return
        }
        // Find the maximum and minimum vertical acceleration values in the window.
        val maxPair = verticalAccWindow.maxByOrNull { it.second }
        val minPair = verticalAccWindow.minByOrNull { it.second }
        if (maxPair == null || minPair == null) return
        val diff = maxPair.second - minPair.second
        // If the difference exceeds the threshold, a step is assumed.
        if (diff < verticalAccDiffThreshold) return

        val currentStepTime = maxPair.first
        if (lastStepTime != 0L && (currentStepTime - lastStepTime) < STEP_TIME_THRESHOLD_MS) {
            Log.i(TAG, "Step ignored due to short time interval.")
            return // Ignore this step
        }
        val stepTimeSec = if (lastStepTime == 0L) 0.5f
        else (currentStepTime - lastStepTime) / 1000f
        lastStepTime = currentStepTime

        // Compute step length using Equation (10):
        // L_step = strideConstant * (fzMax - fzMin)^(1/4)
        val stepLength = strideLength * diff.toDouble().pow(0.25).toFloat()
        Log.i(TAG, "Step detected: length = $stepLength m, time = $stepTimeSec s")

        // Compute step velocity as step length divided by step time.
        val computedStepVelocity =
            if (stepTimeSec > 0f) stepLength / stepTimeSec else stepLength

        Log.i(TAG, "Step velocity: $computedStepVelocity m/s")

        // Update the step positions.
        // The expected displacement is obtained by rotating [0, L_step, 0]^T
        // from the device to the navigation frame.
        val expectedStep = floatArrayOf(0f, stepLength, 0f)
        val expectedStepMatrix = arrayOf(
            floatArrayOf(expectedStep[0]),
            floatArrayOf(expectedStep[1]),
            floatArrayOf(expectedStep[2])
        )
        val observedStepMatrix =
            matrixMultiply(rotationMatrix!!, expectedStepMatrix)
        val displacement = FloatArray(3) { i -> observedStepMatrix[i][0] }

        Log.i(
            TAG,
            "Displacement: ${displacement[0]}, ${displacement[1]}, ${displacement[2]}"
        )

        // Update positions: previous becomes current, and current is advanced.
        previousStepPosition = currentStepPosition.copyOf()
        currentStepPosition = floatArrayOf(
            currentStepPosition[0] + displacement[0],
            currentStepPosition[1] + displacement[1],
            currentStepPosition[2] + displacement[2]
        )

        // Now update the error state with the step length update.
        // Use the maximum and minimum vertical acceleration from the window.
        errorState = kalmanFilter.updateErrorStateWithStepLength(
            errorState,
            currentStepPosition,      // p_k (from step detection)
            previousStepPosition,     // pₖ₋₁ (previous step position)
            maxPair.second,           // fzMax (from the window)
            minPair.second,           // fzMin (from the window)
            rotationMatrix!!,
            deltaTime
        )

        Log.i(
            TAG,
            "Error state updated with step length, errorState = $errorState, deltaTime = $deltaTime, rotationMatrix = $rotationMatrix"
        )

        // Also update the error state with the step velocity update.
        val v_l =
            floatArrayOf(0f, computedStepVelocity, 0f)  // v_l: measured step velocity in nav frame
        val v_i =
            floatArrayOf(0f, computedStepVelocity, 0f)  // v_i: assume zero current velocity (or use measured value)
        errorState =
            kalmanFilter.updateErrorStateWithStepVelocity(errorState, v_l, v_i, rotationMatrix!!)
        verticalAccWindow.clear()
        Log.i(
            "SensorCollector",
            "Step position: ${currentStepPosition[0]}, ${currentStepPosition[1]}, ${currentStepPosition[2]} " +
                    "Step velocity: $computedStepVelocity m/s" +
                    "Step length: $stepLength m" +
                    "Step time: $stepTimeSec s" +
                    "Previous step position: ${previousStepPosition[0]}, ${previousStepPosition[1]}, ${previousStepPosition[2]}" +
                    "Max vertical acceleration: ${maxPair.second}"
        )
        apiDataArray.add(
            mapOf(
                "steplength" to stepLength,
                "velocity" to computedStepVelocity,
                "posx" to currentStepPosition[0],
                "posy" to currentStepPosition[1],
                "posz" to currentStepPosition[2],
                "timestamp" to currentStepTime
            )
        )
        if (apiDataArray.size >= 10) {
            saveFilteredStepData()
        }
    }

    fun saveRawOrientationData() {
        CoroutineScope(Dispatchers.Default).launch {
            val apiData = rawOrientationData.toList()
            rawOrientationData.clear()
            apiHelper.saveData(apiData, API_ENDPOINTS.ORIENTATION_VALUES)
        }
    }

    fun saveRawAccelerationData() {
        CoroutineScope(Dispatchers.Default).launch {
            val apiData = rawAccelerationData.toList()
            rawAccelerationData.clear()
            apiHelper.saveData(apiData, API_ENDPOINTS.ACCELERATION_VALUES)
        }
    }

    fun saveFilteredStepData() {
        val apiData = apiDataArray.toList()
        apiDataArray.clear()
        CoroutineScope(Dispatchers.Default).launch {
            apiHelper.saveData(apiData, API_ENDPOINTS.STEP_EVENTS_VALUES)
        }
    }

    fun saveFilteredAccelerationData() {
        val apiData = filteredAccelerationData.toList()
        filteredAccelerationData.clear()
        CoroutineScope(Dispatchers.Default).launch {
            apiHelper.saveData(apiData, API_ENDPOINTS.ACCELERATION_FILTERED)
        }
    }


    companion object {
        const val STEP_TIME_THRESHOLD_MS = 350L
        const val TAG = "SensorCollector"
    }
}