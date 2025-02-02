package fm.pathfinder.utils

import kotlin.math.sqrt

object Extensions {
    fun FloatArray.norm(): Float {
        var sum = 0f
        for (i in this) {
            sum += i * i
        }
        return sqrt(sum.toDouble()).toFloat()
    }

    fun Array<FloatArray>.flatten2d(): FloatArray {
        return this.flatMap { it.toList() }.toFloatArray()
    }

    fun FloatArray.toColumnMatrix(): Array<FloatArray> {
        return this.map { floatArrayOf(it) }.toTypedArray()
    }

    fun Array<Array<FloatArray>>.flatten3d(): FloatArray {
        return this.flatMap { it.flatMap { row -> row.toList() } }.toFloatArray()
    }

    fun Array<FloatArray>.transpose(): Array<FloatArray> {
        val rows = this.size
        val cols = this[0].size
        val transposed = Array(cols) { FloatArray(rows) }
        for (i in this.indices) {
            for (j in this[i].indices) {
                transposed[j][i] = this[i][j]
            }
        }
        return transposed
    }

}