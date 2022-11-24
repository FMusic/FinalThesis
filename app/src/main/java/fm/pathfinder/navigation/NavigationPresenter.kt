package fm.pathfinder.navigation

import android.content.Context
import fm.pathfinder.data.FileManager
import fm.pathfinder.model.Room

class NavigationPresenter(buildingName: String, ctx: Context) {
    private val buildingData: ArrayList<Room>

    init {
        buildingData = FileManager(ctx).loadDataFromFile(buildingName) as ArrayList<Room>
    }


}
