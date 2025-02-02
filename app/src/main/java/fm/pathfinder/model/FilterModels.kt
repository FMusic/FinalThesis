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

    companion object {
        fun fromVector(vector: FloatArray): ErrorState {
            return ErrorState(
                positionError = vector.sliceArray(0..2),
                velocityError = vector.sliceArray(3..5),
                attitudeError = vector.sliceArray(6..8)
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

    override fun hashCode(): Int {
        var result = gyroscopeBias.contentHashCode()
        result = 31 * result + accelerometerBias.contentHashCode()
        return result
    }
}