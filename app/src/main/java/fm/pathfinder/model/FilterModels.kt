package fm.pathfinder.model

import fm.pathfinder.utils.Extensions.flatten2d
import fm.pathfinder.utils.Extensions.toColumnMatrix


data class ErrorState(
    var positionError: FloatArray = FloatArray(3), // δp
    var velocityError: FloatArray = FloatArray(3), // δv
    var attitudeError: FloatArray = FloatArray(3)  // δa
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ErrorState

        if (!positionError.contentEquals(other.positionError)) return false
        if (!velocityError.contentEquals(other.velocityError)) return false
        if (!attitudeError.contentEquals(other.attitudeError)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = positionError.contentHashCode()
        result = 31 * result + velocityError.contentHashCode()
        result = 31 * result + attitudeError.contentHashCode()
        return result
    }

    fun toVector(): Array<FloatArray> {
        return arrayOf(
            positionError,
            velocityError,
            attitudeError
        ).map { it.toColumnMatrix().flatten2d() }.toTypedArray()
    }


    // Inside ErrorState
    fun toVector9x1(): Array<FloatArray> {
        val vec = Array(9) { FloatArray(1) }
        // positionError -> indices [0..2]
        vec[0][0] = positionError[0]
        vec[1][0] = positionError[1]
        vec[2][0] = positionError[2]
        // velocityError -> indices [3..5]
        vec[3][0] = velocityError[0]
        vec[4][0] = velocityError[1]
        vec[5][0] = velocityError[2]
        // attitudeError -> indices [6..8]
        vec[6][0] = attitudeError[0]
        vec[7][0] = attitudeError[1]
        vec[8][0] = attitudeError[2]
        return vec
    }

    companion object {
        fun fromVector(vector: FloatArray): ErrorState {
            return ErrorState(
                positionError = vector.sliceArray(0..2),
                velocityError = vector.sliceArray(3..5),
                attitudeError = vector.sliceArray(6..8)
            )
        }

        fun fromVector9x1(vec: Array<FloatArray>): ErrorState {
            return ErrorState(
                floatArrayOf(vec[0][0], vec[1][0], vec[2][0]),
                floatArrayOf(vec[3][0], vec[4][0], vec[5][0]),
                floatArrayOf(vec[6][0], vec[7][0], vec[8][0])
            )
        }
    }
}

data class SensorBias(
    var gyroscopeBias: FloatArray = FloatArray(3), // bg
    var accelerometerBias: FloatArray = FloatArray(3) // bf
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorBias

        if (!gyroscopeBias.contentEquals(other.gyroscopeBias)) return false
        if (!accelerometerBias.contentEquals(other.accelerometerBias)) return false

        return true
    }

    // In SensorBias:
    fun toVector6x1(): Array<FloatArray> {
        // b_s = [ b_g, b_f ] in a 6×1
        val vec = Array(6) { FloatArray(1) }
        // gyroscopeBias -> first 3
        vec[0][0] = gyroscopeBias[0]
        vec[1][0] = gyroscopeBias[1]
        vec[2][0] = gyroscopeBias[2]
        // accelerometerBias -> next 3
        vec[3][0] = accelerometerBias[0]
        vec[4][0] = accelerometerBias[1]
        vec[5][0] = accelerometerBias[2]
        return vec
    }


    override fun hashCode(): Int {
        var result = gyroscopeBias.contentHashCode()
        result = 31 * result + accelerometerBias.contentHashCode()
        return result
    }
}