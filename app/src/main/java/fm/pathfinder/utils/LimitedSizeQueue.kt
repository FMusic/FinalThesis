package fm.pathfinder.utils

import android.util.Log
import java.util.ArrayList
import java.util.stream.Collectors
import kotlin.math.absoluteValue

class LimitedSizeQueue<K : Number>(val maxSize: Int) : ArrayList<K>() {
    override fun add(element: K): Boolean {
        val r = super.add(element)
        if (spaceLeft() < 0) {
            removeRange(0, spaceLeft().absoluteValue)
        }
        return r
    }

    private fun spaceLeft() = this.maxSize - this.size

    fun average(): Double {
        return this.stream().collect(Collectors.averagingDouble { it.toDouble() })
    }
}