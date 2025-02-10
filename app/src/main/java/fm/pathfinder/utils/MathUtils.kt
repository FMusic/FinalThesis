package fm.pathfinder.utils

import java.util.stream.Collectors
import kotlin.math.absoluteValue

object MathUtils {
    fun identityMatrix(size: Int): Array<FloatArray> {
        return Array(size) { i -> FloatArray(size) { j -> if (i == j) 1f else 0f } }
    }

    fun zeroMatrix(rows: Int, cols: Int): Array<FloatArray> {
        return Array(rows) { FloatArray(cols) { 0f } }
    }

    fun skewSymmetric(vector: FloatArray): Array<FloatArray> {
        return arrayOf(
            floatArrayOf(0f, -vector[2], vector[1]),
            floatArrayOf(vector[2], 0f, -vector[0]),
            floatArrayOf(-vector[1], vector[0], 0f)
        )
    }

    fun matrixMultiply(a: Array<FloatArray>, b: Array<FloatArray>): Array<FloatArray> {
        val rowsA = a.size
        val colsA = a[0].size
        val colsB = b[0].size
        val result = Array(rowsA) { FloatArray(colsB) }

        for (i in 0 until rowsA) {
            for (j in 0 until colsB) {
                var sum = 0f
                for (k in 0 until colsA) {
                    sum += a[i][k] * b[k][j]
                }
                result[i][j] = sum
            }
        }
        return result
    }

    fun crossProductUsingSkewSymmetric(a: FloatArray, b: FloatArray): FloatArray {
        return floatArrayOf(
            a[1] * b[2] - a[2] * b[1],
            a[2] * b[0] - a[0] * b[2],
            a[0] * b[1] - a[1] * b[0]
        )
    }

    fun Array<FloatArray>.invertMatrix(): Array<FloatArray> {
        val n = this.size
        // Create an augmented matrix [matrix | I].
        val augmented = Array(n) { i ->
            FloatArray(2 * n) { j ->
                when {
                    j < n -> this[i][j]
                    j - n == i -> 1f
                    else -> 0f
                }
            }
        }

        // Perform Gauss-Jordan elimination.
        for (i in 0 until n) {
            // Find pivot.
            var pivot = augmented[i][i]
            if (pivot == 0f) {
                // Swap with a row below that has a nonzero pivot.
                for (j in i + 1 until n) {
                    if (augmented[j][i] != 0f) {
                        val temp = augmented[i]
                        augmented[i] = augmented[j]
                        augmented[j] = temp
                        pivot = augmented[i][i]
                        break
                    }
                }
                if (pivot == 0f) {
                    throw IllegalArgumentException("Matrix is singular and cannot be inverted.")
                }
            }
            // Normalize the pivot row.
            for (j in 0 until 2 * n) {
                augmented[i][j] /= pivot
            }
            // Eliminate the pivot column in other rows.
            for (r in 0 until n) {
                if (r != i) {
                    val factor = augmented[r][i]
                    for (j in 0 until 2 * n) {
                        augmented[r][j] -= factor * augmented[i][j]
                    }
                }
            }
        }

        // Extract the inverse matrix (right half of the augmented matrix).
        return Array(n) { i ->
            FloatArray(n) { j -> augmented[i][j + n] }
        }
    }

    fun addMatrices(a: Array<FloatArray>, b: Array<FloatArray>): Array<FloatArray> {
        val rows = a.size
        val cols = a[0].size
        return Array(rows) { i ->
            FloatArray(cols) { j -> a[i][j] + b[i][j] }
        }
    }

    fun subtractMatrices(a: Array<FloatArray>, b: Array<FloatArray>): Array<FloatArray> {
        val rows = a.size
        val cols = a[0].size
        return Array(rows) { i ->
            FloatArray(cols) { j -> a[i][j] - b[i][j] }
        }
    }
    /**
     * Converts a FloatArray into a column matrix (an Array of FloatArrays with one element each).
     */
    fun FloatArray.toColumnMatrix(): Array<FloatArray> =
        Array(this.size) { i -> floatArrayOf(this[i]) }

}

class LimitedSizeQueue<K : Number>(private val maxSize: Int) : ArrayList<K>() {
    override fun add(element: K): Boolean {
        val r = super.add(element)
        if (spaceLeft() < 0) {
            removeRange(0, spaceLeft().absoluteValue)
        }
        return r
    }

    private fun spaceLeft() = this.maxSize - this.size

    fun isFull(): Boolean = spaceLeft() <= 0

    fun average(): Double {
        return this.stream().collect(Collectors.averagingDouble { it.toDouble() })
    }
}
