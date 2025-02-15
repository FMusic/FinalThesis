package fm.pathfinder.utils

import fm.pathfinder.utils.Extensions.norm
import kotlin.math.absoluteValue

data class MaxMinPair(
    var max: Double,
    var min: Double,
    var timestampMax: Long,
    var timestampMin: Long
)

class Calibrator {
    private var stepsDetected = 0
    private var stepMaxMinAcceleration = ArrayList<MaxMinPair>()
    private var minAcceleration = Pair(Double.MAX_VALUE, 0L)
    private val last5Averages = LimitedSizeQueue<Double>(5)
    private val last100Averages = LimitedSizeQueue<Double>(100)
    private val last5Acceleration = LimitedSizeQueue<Double>(5)
    private var stepDetected: Boolean = false

    fun addAcceleration(acceleration: FloatArray, timestamp: Long) {
        val currentNorm = acceleration.norm().toDouble()
        last5Acceleration.add(currentNorm)
        if (minAcceleration.first > currentNorm) {
            minAcceleration = Pair(currentNorm, timestamp)
        }
        if (last5Acceleration.size == 5) {
            val average = last5Acceleration.average()
            last5Averages.add(average)
            last100Averages.add(average)
            if (last5Averages.average() > last100Averages.average()) {
                if (!stepDetected) {
                    stepDetected = true
                    stepsDetected++
                    val last5AccAvg = last5Acceleration.average()
                    val newMaxMinPair = MaxMinPair(
                        last5AccAvg,
                        minAcceleration.first,
                        timestamp,
                        minAcceleration.second
                    )
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
            val timestampMax = stepMaxMinAcceleration[i].timestampMax
            val timestampMin = stepMaxMinAcceleration[i].timestampMin
            val dtSeconds = (timestampMax - timestampMin).absoluteValue / 1_000_000_000.0
            distanceCoveredSensor += (accelerationDifference / 2.0) * dtSeconds * dtSeconds

        }
        return distanceCoveredSensor / length
    }
}