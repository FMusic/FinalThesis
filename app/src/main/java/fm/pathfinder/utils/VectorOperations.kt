package fm.pathfinder.utils

import kotlin.math.pow
import kotlin.math.sqrt

object VectorOperations {
    fun normalizeVector(vector: FloatArray): Float {
        val magnitude = sqrt(vector[0].pow(2) + vector[1].pow(2) + vector[2].pow(2))
        return magnitude
    }
}