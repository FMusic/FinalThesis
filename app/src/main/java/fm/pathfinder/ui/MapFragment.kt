package fm.pathfinder.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fm.pathfinder.R
import fm.pathfinder.utils.InputDialogFragment

/**
 * tutorial used: https://www.thecrazyprogrammer.com/2017/01/how-to-get-current-location-in-android.html
 */
class MapFragment : Fragment() {
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var currentLocation: LatLng
    private lateinit var mapPresenter: MapPresenter

    private lateinit var tvLogger: TextView
    private lateinit var btnStopScan: Button
    private lateinit var btnNewRoom: Button
    private lateinit var btnExitRoom: Button
    private lateinit var btnStep: Button
    private lateinit var rowCalibration: View
    private lateinit var rowWork: View
    private lateinit var btnCalibration: Button

    private val textForLog = StringBuilder()


    private val callback = OnMapReadyCallback { googleMap ->
        if (this::currentLocation.isInitialized) {
            googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        currentLocation.latitude,
                        currentLocation.longitude
                    )
                ) // Default position
                .zoom(17f) // Default zoom level
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MapFragment().apply {
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onResume() {
        super.onResume()
        mapPresenter = MapPresenter(this, requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapPresenter = MapPresenter(this, requireContext())
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(callback)

        rowCalibration = view.findViewById(R.id.rowCalibration)
        rowWork = view.findViewById(R.id.rowWork)

        btnCalibration = view.findViewById(R.id.btnCalibration)
        btnStopScan = view.findViewById(R.id.btnStopScan)
        btnNewRoom = view.findViewById(R.id.btnNewRoom)
        btnExitRoom = view.findViewById(R.id.btnExitRoom)
        btnStep = view.findViewById(R.id.btnStep)
        tvLogger = view.findViewById(R.id.tvLog)

        btnCalibration.setOnClickListener { onCalibrationClick() }
        btnStopScan.setOnClickListener { onStopScanClick() }
        btnNewRoom.setOnClickListener { onNewRoomClick() }
        btnExitRoom.setOnClickListener { onExitRoomClick() }
        btnStep.setOnClickListener { mapPresenter.newStepClick() }
    }

    private fun onCalibrationClick() {
        if (btnCalibration.text == getString(R.string.calibration)) {
            mapPresenter.startCalibration()
            btnCalibration.text = getString(R.string.stop_calibration)
            addTextToTvLog(getString(R.string.calibration_started))
        } else {                       // “Stop Calibration” pressed
            mapPresenter.startScan()

            rowCalibration.visibility = View.GONE
            rowWork.visibility = View.VISIBLE
            addTextToTvLog(getString(R.string.calibration_stopped_scan_started))
        }
    }

    private fun onStopScanClick() {
        mapPresenter.stopScan("pathfinder")
        rowCalibration.visibility = View.VISIBLE
        rowWork.visibility = View.GONE
        btnCalibration.text = getText(R.string.calibration)
    }

    private fun onNewRoomClick() = showNewRoomDialog()

    private fun onExitRoomClick() {
        mapPresenter.exitRoom()
        btnNewRoom.visibility = View.VISIBLE
        btnExitRoom.visibility = View.INVISIBLE
    }

    private fun showNewRoomDialog() {
        val inputDialog = InputDialogFragment()
            .setTitle("New room label")
            .setOnInputCompleteListener {
                mapPresenter.newRoom(it)
                btnNewRoom.visibility = View.INVISIBLE
                btnExitRoom.visibility = View.VISIBLE
            }
        inputDialog.show(childFragmentManager, "InputDialog")
    }

    fun changeLocation(location: LatLng) {
        currentLocation = location
        addTextToTvLog(
            "GPS: Lat: ${coerceLocLat(location.latitude)} " +
                    "Long: ${coerceLocLat(location.longitude)}\n"
        )
        if (!this::mapFragment.isInitialized) {
            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        }
        mapFragment.getMapAsync(callback)
    }

    private fun addTextToTvLog(txt: String) {
        textForLog.append(txt)
        textForLog.appendLine()
        if (textForLog.lines().size > 15) {
            textForLog.delete(0, textForLog.indexOf("\n") + 1)
            textForLog.capacity()
        }
        if (!this::tvLogger.isInitialized) {
            tvLogger = requireView().findViewById(R.id.tvLog)
        }
        tvLogger.text = textForLog.toString()

    }

    private fun coerceLocLat(double: Double) =
        double.toString().substring(0, double.toString().length.coerceAtMost(10))

    fun logIt(s: String) {
        addTextToTvLog(s)
    }
}