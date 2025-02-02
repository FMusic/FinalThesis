package fm.pathfinder.utils

import fm.pathfinder.utils.Extensions.norm
import java.sql.Timestamp

data class MaxMinPair(
    var max: Double,
    var min: Double,
    var timestampMax: Timestamp,
    var timestampMin: Timestamp
)

class Calibrator {
    private var stepsDetected = 0
    private var stepMaxMinAcceleration = ArrayList<MaxMinPair>()
    private val last5Averages = LimitedSizeQueue<Double>(5)
    private val last100Averages = LimitedSizeQueue<Double>(100)
    private val last5Acceleration = LimitedSizeQueue<Double>(5)
    private var stepDetected: Boolean = false

    fun addAcceleration(acceleration: FloatArray, timestamp: Timestamp) {
        val currentNorm = acceleration.norm().toDouble()
        if (last5Averages.size == 5) {
            last5Acceleration.add(currentNorm)
            val average = last5Acceleration.average()
            last5Averages.add(average)
            if (last5Averages.average() > last100Averages.average()) {
                if (!stepDetected) {
                    stepDetected = true
                    stepsDetected++
                    val last5AccAvg = last5Acceleration.average()
                    val newMaxMinPair = MaxMinPair(last5AccAvg, last5AccAvg, timestamp, timestamp)
                    stepMaxMinAcceleration.add(newMaxMinPair)
                } else {
                    stepMaxMinAcceleration[stepsDetected - 1].max = last5Acceleration.average()
                    stepMaxMinAcceleration[stepsDetected - 1].timestampMax = timestamp
                }
            } else if (last5Averages.average() < last100Averages.average()) {
                stepDetected = false
            }
        }
    }

    fun stepLength(length: Int): Double {
        var distanceCoveredSensor = 0.0
        for (i in 0 until stepMaxMinAcceleration.size) {
            val accelerationDifference =
                stepMaxMinAcceleration[i].max - stepMaxMinAcceleration[i].min
            distanceCoveredSensor += accelerationDifference / 2 * (stepMaxMinAcceleration[i].timestampMax.time - stepMaxMinAcceleration[i].timestampMin.time)
        }
        return distanceCoveredSensor / length
    }
}