package fm.pathfinder.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import fm.pathfinder.model.Building
import fm.pathfinder.model.Vector
import fm.pathfinder.sensor.GpsSensor
import fm.pathfinder.sensor.Sensors
import fm.pathfinder.utils.LimitedSizeQueue

class MapPresenter(
    private val mapFragment: MapFragment,
    private val context: Context
) : Building.Event {
    companion object {
        private const val TAG: String = "MapPresenter"
        private const val MIN_CHART_SIZE = 100
    }

    private val building: Building = Building()
    private var sensors = Sensors(context, building)
    private var scanningOn = false

    private val chartEntries = LimitedSizeQueue<Int>(MIN_CHART_SIZE)

    init {
        building.subscribe(this)
        GpsSensor(context, building, this::onNewGpsPoint)
    }

    fun startCalibration() {
        sensors.setCalibration(true)
    }

    fun onNewGpsPoint(location: LatLng){
        mapFragment.changeLocation(location)
    }

    fun startScan() {
        scanningOn = true
        sensors.setScan(true)
        building.setScan(true)
        sensors.setCalibration(false)
    }

    fun stopScan(filename: String) {
        scanningOn = false
        sensors.setScan(false)
        building.setScan(false)
//        DataStoragePresenter(context).storeBuildingToFile(building, filename)
        Toast.makeText(context, "Saving building data", Toast.LENGTH_SHORT).show()
    }

    fun newRoom(roomName: String) {
        building.enterNewRoom(roomName)
    }

    fun exitRoom() {
        building.exitRoom()
    }

    override fun onNewPoint(point: Vector) {
        Log.i(TAG, "New point: $point")
        mapFragment.logIt("New point: $point")
    }

//    fun setLineChart() {
//        val dataSet = if (chartEntries.isEmpty()) {
//            // dummy data
//            (0..MIN_CHART_SIZE).map { Entry(it.toFloat(), it.toFloat()) }
//        } else {
//            chartEntries.mapIndexed { index, acceleration ->
//                Entry(
//                    index.toFloat(),
//                    acceleration.toFloat()
//                )
//            }
//        }
//        val lineDataSet = LineDataSet(dataSet, "Label for Data") // Set the label here
//        val lineData = LineData(lineDataSet)
//        mapFragment.lineChart.data = lineData
//        mapFragment.lineChart.notifyDataSetChanged()
//        mapFragment.lineChart.invalidate()
//    }

//    fun acceleration(acc: Acceleration) {
//        chartEntries.add(acc.norm().toInt())
//        setLineChart()
//    }

    fun newStep(distance: Float) {
        Log.i(TAG, "New step: $distance")
        mapFragment.logIt("New step: $distance")
    }

    fun orientation(orientation: Float) {
//        mapFragment.tvOrientation.text = orientation.toString()
    }

    fun newStepClick() {
        sensors.newStep()
    }

}