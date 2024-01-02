package fm.pathfinder.ui

import android.content.Context
import fm.pathfinder.model.MapLine
import fm.pathfinder.model.Room
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class NavigationPresenter(buildingName: String, ctx: Context) {
    private var originalMap: ArrayList<MapLine> = ArrayList()
    private val buildingData: ArrayList<Room>

    init {
        buildingData = DataStoragePresenter(ctx).loadDataFromFile(buildingName) as ArrayList<Room>
        originalMap = parseOut(buildingData)
    }

    private fun parseOut(buildingData: java.util.ArrayList<Room>): java.util.ArrayList<MapLine> {
        val map = ArrayList<MapLine>()
        var begX = 0F
        var begY = 0F
        buildingData.forEach { room ->
            room.listOfSpots.forEach { spot ->
                val newX = begX +
                    spot.distance.times(cos(spot.movementDirection)).toFloat()
                val newY = begY +
                    spot.distance.times(sin(spot.movementDirection)).toFloat()
                map.add(MapLine(begX, begY, newX, newY))
                begX = newX
                begY = newY
            }
        }
        return map
    }

    fun prepareAndScaleMap(height: Int, width: Int): ArrayList<MapLine> {
        val lastMap = ArrayList<MapLine>()
        val lastSpot = originalMap[originalMap.size - 1]
        val scaleX = height.div(lastSpot.stopX.absoluteValue)
        val scaleY = width.div(lastSpot.stopY.absoluteValue)
        val scale = if (scaleX < scaleY)
            scaleX
        else
            scaleY

        originalMap.forEach {
            lastMap.add(
                MapLine(
                    it.startX.absoluteValue.times(scale),
                    it.startY.absoluteValue.times(scale),
                    it.stopX.absoluteValue.times(scale),
                    it.stopY.absoluteValue.times(scale)
                )
            )
        }
        return lastMap
    }


}
