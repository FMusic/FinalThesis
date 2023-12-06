package fm.pathfinder.utils

import fm.pathfinder.model.Vector

class Kalman(private val dimensions : Int = 3) {

    fun measure(point: Vector): Vector {
        predict()
        return point
    }

    private fun predict(){

    }
}