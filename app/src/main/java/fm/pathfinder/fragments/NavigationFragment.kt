package fm.pathfinder.fragments

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import fm.pathfinder.Constants
import fm.pathfinder.R
import fm.pathfinder.model.MapLine
import fm.pathfinder.navigation.NavigationPresenter


/**
 * A simple [Fragment] subclass.
 * Use the [NavigationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NavigationFragment : Fragment(), SurfaceHolder.Callback {
    private lateinit var holder: SurfaceHolder
    private lateinit var navigationPresenter: NavigationPresenter
    private val mPaint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.FILL
    }

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
        val inflatedLayout = inflater.inflate(R.layout.fragment_navigation, container, false)
        val surface = inflatedLayout.findViewById<SurfaceView>(R.id.viewMap)
        surface.setBackgroundColor(Color.WHITE)
        surface.setZOrderOnTop(true)
        holder = surface.holder
        holder.addCallback(this)
        return inflatedLayout
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param buildingName File name of building to load.
         * @return A new instance of fragment NavigationFragment.
         */
        @JvmStatic
        fun newInstance(buildingName: String) =
            NavigationFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.BUILDING_NAME_PARAM, buildingName)
                }
            }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val canvas = holder.lockCanvas()

        val origigiMap = navigationPresenter.prepareAndScaleMap(canvas.height, canvas.width)

        origigiMap.forEach {
            canvas.drawLine(it.startX, it.startY, it.stopX, it.stopY, mPaint)
        }
        holder.unlockCanvasAndPost(canvas)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    fun addToSurface(mapLine: MapLine){
        val can = holder.lockCanvas()
        can.drawLine(mapLine.startX, mapLine.startY, mapLine.stopX, mapLine.stopY, mPaint)
        holder.unlockCanvasAndPost(can)

    }

}