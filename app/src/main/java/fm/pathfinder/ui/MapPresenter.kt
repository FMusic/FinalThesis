package fm.pathfinder.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import fm.pathfinder.model.Acceleration
import fm.pathfinder.model.Vector
import fm.pathfinder.sensor.SensorCollector
import fm.pathfinder.model.Building
import fm.pathfinder.utils.LimitedSizeQueue

class MapPresenter(
    private val mapFragment: MapFragment,
    private val context: Context
): Building.Event{
    companion object {
        private const val TAG: String = "MapPresenter"
        private const val MIN_CHART_SIZE = 100
    }
    private val building: Building = Building()
    private var sensorCollector = SensorCollector(context, building, this)
    private var scanningOn = false

    private val chartEntries = LimitedSizeQueue<Int>(MIN_CHART_SIZE)

    init {
        building.subscribe(this)
        sensorCollector.initializeSensors()
    }

    fun startCalibration() {
        sensorCollector.setCalibration(true)
    }

    fun startScan() {
        scanningOn = true
        sensorCollector.setScan(true)
        building.setScan(true)
    }

    fun stopScan(filename: String) {
        scanningOn = false
        sensorCollector.setScan(false)
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

    fun setLineChart() {
        val dataSet = if (chartEntries.isEmpty()) {
            // dummy data
            (0..MIN_CHART_SIZE).map { Entry(it.toFloat(), it.toFloat()) }
        } else {
            chartEntries.mapIndexed { index, acceleration -> Entry(index.toFloat(), acceleration.toFloat()) }
        }
        val lineDataSet = LineDataSet(dataSet, "Label for Data") // Set the label here
        val lineData = LineData(lineDataSet)
        mapFragment.lineChart.data = lineData
        mapFragment.lineChart.notifyDataSetChanged()
        mapFragment.lineChart.invalidate()
    }

    fun acceleration(acc: Acceleration){
        chartEntries.add(acc.norm().toInt())
        setLineChart()
    }

    fun newStep(distance: Float) {
        Log.i(TAG, "New step: $distance")
        mapFragment.logIt("New step: $distance")
    }

    fun orientation(orientation: Float) {
        mapFragment.tvOrientation.text = orientation.toString()
    }

}