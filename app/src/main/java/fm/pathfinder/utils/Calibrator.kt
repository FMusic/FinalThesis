package fm.pathfinder.utils

import android.util.Log
import fm.pathfinder.utils.Extensions.norm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

data class MaxMinPair(
    var max: Double,
    var min: Double,
    var timestampMax: Long,
    var timestampMin: Long
)

/**
 * A calibrator using a finite-state approach to find local peak/valley
 * pairs from accelerometer data, then compute step lengths.
 */
class Calibrator {

    // For storing each (max, min) pair we detect
    private val stepMaxMinAcceleration = ArrayList<MaxMinPair>()

    // We still use these limited-size queues:
    private val last5Acceleration = LimitedSizeQueue<Double>(5)
    private val last5Averages = LimitedSizeQueue<Double>(5)
    private val last100Averages = LimitedSizeQueue<Double>(100)

    // A small state machine for step detection
    private enum class StepState { IDLE, RISING, PEAK }

    private var stepState = StepState.IDLE

    // We'll keep track of a local peak (max) and local valley (min) for each step cycle
    private var localPeak = 0.0
    private var localPeakTime = 0L
    private var localValley = 9.81   // approximate baseline or use long-term average
    private var localValleyTime = 0L

    // Simple thresholds (tune these!)
    private val THRESH_RISE = 0.5   // how much above baseline is "rising"?
    private val THRESH_FALL = 0.5   // how much below the peak is "falling"?
    private val MIN_PEAK_DIFF = 1.0   // minimal (peak - valley) to accept a step
    private val apiHelper = ApiHelper()

    /**
     * Called for every new accelerometer reading.
     * We insert the magnitude (norm) into short-term smoothing,
     * compute short & long average, then feed the result into our state machine.
     */
    fun addAcceleration(acceleration: FloatArray, timestamp: Long) {
        // 1) Add raw norm to the short-term queue
        val currentNorm = acceleration.norm().toDouble()
        last5Acceleration.add(currentNorm)

        // 2) Once we have 5 samples in last5Acceleration, we get a short-term average
        if (last5Acceleration.size == 5) {
            val shortTermAvg = last5Acceleration.average()
            last5Averages.add(shortTermAvg)

            // For the "baseline," we can also keep a longer queue average
            last100Averages.add(shortTermAvg)
            val baseline = last100Averages.average()  // or keep a slow updating baseline

            // 3) Pass the short-term average + baseline + timestamp to our step FSM
            updateStateMachine(shortTermAvg, baseline, timestamp)
        }
    }

    /**
     * A finite-state machine to detect a local peak followed by a local valley.
     */
    private fun updateStateMachine(avgAcc: Double, baseline: Double, timestamp: Long) {
        when (stepState) {
            StepState.IDLE -> {
                // If short-term average is above baseline + THRESH_RISE, we start RISING
                if (avgAcc > baseline + THRESH_RISE) {
                    stepState = StepState.RISING
                    localPeak = avgAcc
                    localPeakTime = timestamp
                    // Initialize valley near the baseline
                    localValley = baseline
                    localValleyTime = timestamp
                }
            }

            StepState.RISING -> {
                // If it's still going up, update local peak
                if (avgAcc > localPeak) {
                    localPeak = avgAcc
                    localPeakTime = timestamp
                }
                // If it falls by THRESH_FALL from the local peak, we confirm a peak
                else if ((localPeak - avgAcc) > THRESH_FALL) {
                    stepState = StepState.PEAK
                    // Start the valley from the current measurement
                    localValley = avgAcc
                    localValleyTime = timestamp
                }
            }

            StepState.PEAK -> {
                // Now we watch for the local valley
                // If we see an even lower acceleration, update localValley
                if (avgAcc < localValley) {
                    localValley = avgAcc
                    localValleyTime = timestamp
                }
                // If we start rising again above localValley + THRESH_RISE,
                // we finalize the peak/valley pair.
                else if (avgAcc > localValley + THRESH_RISE) {
                    // Only record if peak - valley is big enough
                    val diff = localPeak - localValley
                    if (diff >= MIN_PEAK_DIFF) {
                        // Record a new MaxMinPair
                        stepMaxMinAcceleration.add(
                            MaxMinPair(
                                max = localPeak,
                                min = localValley,
                                timestampMax = localPeakTime,
                                timestampMin = localValleyTime
                            )
                        )
                    }
                    // Reset to IDLE to look for next step
                    stepState = StepState.IDLE
                }
            }
        }
    }

    /**
     * After collecting steps, compute a "step length" for calibration.
     * Suppose we told the user to walk 'length' meters in total.
     * We use the sum of (max - min) from each step to guess distance,
     * or do something else. Tweak as needed.
     */
    fun stepLength(length: Int): Double {
        saveAccelerationData()
        var distanceCoveredSensor = 0.0
        for (pair in stepMaxMinAcceleration) {
            val accelerationDifference = (pair.max - pair.min)
            val distanceCovered =
                (accelerationDifference / 2) * abs(pair.timestampMax - pair.timestampMin)
            // You might also use (pair.timestampMax, pair.timestampMin) for time-based logic
            distanceCoveredSensor += distanceCovered
            Log.i(
                TAG, "Distance covered: $distanceCovered, " +
                        "Time: ${pair.timestampMax - pair.timestampMin}"
            )
        }
        Log.i(
            TAG, "Total distance covered: $distanceCoveredSensor, " +
                    "Stride length: ${length / distanceCoveredSensor}"
        )
        return if (stepMaxMinAcceleration.isEmpty()) 0.0
        else (length / distanceCoveredSensor)
    }

    private fun saveAccelerationData() {
        CoroutineScope(Dispatchers.Default).launch {
            val apiData = stepMaxMinAcceleration.map {
                mapOf(
                    "maxacc" to it.max,
                    "minacc" to it.min,
                    "timestampmax" to it.timestampMax,
                    "timestampmin" to it.timestampMin
                )
            }.toList()
            apiHelper.saveData(apiData, API_ENDPOINTS.CALIBRATOR_VALUES)
        }
    }

    companion object {
        const val TAG = "Calibrator"
    }
}