package fm.pathfinder.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import fm.pathfinder.R
import fm.pathfinder.presenters.MapPresenter

class MapsFragment : Fragment() {
    /**
     * tutorial used: https://www.thecrazyprogrammer.com/2017/01/how-to-get-current-location-in-android.html
     */
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var currentLocation: LatLng
    private lateinit var mapPresenter: MapPresenter
    private lateinit var tvLogger: TextView


    private val callback = OnMapReadyCallback { googleMap ->
        if (this::currentLocation.isInitialized) {
            googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MapsFragment().apply {

            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapPresenter = MapPresenter(this)

        val btnStartScan = view.findViewById<Button>(R.id.btnStartScan)
        val btnStopScan = view.findViewById<Button>(R.id.btnStopScan)
        val btnNewRoom = view.findViewById<Button>(R.id.btnNewRoom)
        tvLogger = view.findViewById(R.id.tvLog)

        btnStartScan.setOnClickListener {
            mapPresenter.startScan()
            btnStartScan.visibility = View.INVISIBLE
            btnStopScan.visibility = View.VISIBLE
            btnNewRoom.visibility = View.VISIBLE
        }

        btnStopScan.setOnClickListener {
            mapPresenter.stopScan()
            btnStartScan.visibility = View.VISIBLE
            btnStopScan.visibility = View.INVISIBLE
            btnNewRoom.visibility = View.INVISIBLE
        }

        btnNewRoom.setOnClickListener {
            alerterNewRoom()
        }
    }

    private fun alerterNewRoom() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("New room label")
        val viewInfl = LayoutInflater.from(context)
            .inflate(R.layout.new_room_input_dialog, view as ViewGroup, false)
        val input = viewInfl.findViewById<EditText>(R.id.etNewRoomLabel)
        builder.setView(viewInfl)

        builder.setPositiveButton(
            android.R.string.ok
        ) { dialog, _ ->
            dialog.dismiss()
            mapPresenter.newRoom(input.text.toString())
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
            dialog.cancel()
        }
        builder.show()

    }

    fun changeLocation(location: LatLng) {
        currentLocation = location
        tvLogger.text = "GPS: Lat: ${location.latitude} Long: ${location.longitude}"
        mapFragment.getMapAsync(callback)
    }
}