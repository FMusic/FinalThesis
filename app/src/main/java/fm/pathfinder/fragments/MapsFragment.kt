package fm.pathfinder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val callback = OnMapReadyCallback { googleMap ->
        if(this::currentLocation.isInitialized){
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
        MapPresenter(this)
    }

    fun changeLocation(location: LatLng) {
        currentLocation = location
        mapFragment.getMapAsync(callback)
    }
}