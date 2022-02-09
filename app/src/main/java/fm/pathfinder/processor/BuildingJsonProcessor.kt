package fm.pathfinder.processor

import com.google.gson.Gson
import fm.pathfinder.model.Building

object BuildingJsonProcessor {
    fun generateJsonFrom(building:Building): String{
        return Gson().toJson(building)
    }
}