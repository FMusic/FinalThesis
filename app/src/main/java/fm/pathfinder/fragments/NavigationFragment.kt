package fm.pathfinder.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import fm.pathfinder.Constants
import fm.pathfinder.views.MapDrawable
import fm.pathfinder.R
import fm.pathfinder.navigation.NavigationPresenter


/**
 * A simple [Fragment] subclass.
 * Use the [NavigationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NavigationFragment : Fragment() {
    private lateinit var navigationPresenter: NavigationPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val buildingName = it.getString(Constants.BUILDING_NAME_PARAM)
            navigationPresenter = NavigationPresenter(buildingName!!, requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val a = inflater.inflate(R.layout.fragment_navigation, container, false)
        var mapDrawing = MapDrawable()
        var img = a.findViewById<ImageView>(R.id.viewMap)
        img.setImageDrawable(mapDrawing)
        return a
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param buildingName File name of building to load.
         * @return A new instance of fragment NavigationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(buildingName: String) =
            NavigationFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.BUILDING_NAME_PARAM, buildingName)
                }
            }
    }
}