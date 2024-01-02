package fm.pathfinder.model

import androidx.annotation.StringRes
import fm.pathfinder.R


class Azimuth(_degrees: Float) {

    operator fun plus(degrees: Float) = Azimuth(this.degrees + degrees)

    val degrees = normalizeAngle(_degrees)

    private val cardinalDirection: CardinalDirection = when (degrees) {
        in 22.5f until 67.5f -> CardinalDirection.NORTHEAST
        in 67.5f until 112.5f -> CardinalDirection.EAST
        in 112.5f until 157.5f -> CardinalDirection.SOUTHEAST
        in 157.5f until 202.5f -> CardinalDirection.SOUTH
        in 202.5f until 247.5f -> CardinalDirection.SOUTHWEST
        in 247.5f until 292.5f -> CardinalDirection.WEST
        in 292.5f until 337.5f -> CardinalDirection.NORTHWEST
        else -> CardinalDirection.NORTH
    }

    private fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        if (degrees != other.degrees) return false

        return true
    }

    override fun hashCode(): Int {
        return degrees.hashCode()
    }

    override fun toString(): String {
        return "Azimuth(degrees=$degrees, direction=$cardinalDirection)"
    }

    enum class CardinalDirection(@StringRes val labelResourceId: Int) {
        NORTH(R.string.cardinal_direction_north),
        NORTHEAST(R.string.cardinal_direction_northeast),
        EAST(R.string.cardinal_direction_east),
        SOUTHEAST(R.string.cardinal_direction_southeast),
        SOUTH(R.string.cardinal_direction_south),
        SOUTHWEST(R.string.cardinal_direction_southwest),
        WEST(R.string.cardinal_direction_west),
        NORTHWEST(R.string.cardinal_direction_northwest)
    }
}

private data class SemiClosedFloatRange(val fromInclusive: Float, val toExclusive: Float)
private operator fun SemiClosedFloatRange.contains(value: Float) = fromInclusive <= value && value < toExclusive
private infix fun Float.until(to: Float) = SemiClosedFloatRange(this, to)

