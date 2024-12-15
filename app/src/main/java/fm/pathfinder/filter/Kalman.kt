package fm.pathfinder.filter

class Kalman(private val dimensions : Int = 3) {

    fun measure(point: Float): Float {
        predict()
        return point
    }

    private fun predict(){

    }
}