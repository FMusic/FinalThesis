package fm.pathfinder.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import fm.pathfinder.R
import fm.pathfinder.activities.MainActivity

class MainMenuFragment : Fragment(R.layout.fragment_main_menu) {
    /**
     * fragment changer is main activity which handles all fragment changes
     */
    lateinit var fragmentChanger: FragmentChangeListener

    companion object {
        @JvmStatic
        fun newInstance(mainActivity: MainActivity) =
            MainMenuFragment().apply {
                fragmentChanger = mainActivity
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_main_menu, container, false)
        val btnStartScan = v.findViewById<Button>(R.id.btnStartScan)
        val btnOpenData = v.findViewById<Button>(R.id.btnOpenData)

        btnStartScan.setOnClickListener { fragmentChanger.changeFragment(1) }
        btnOpenData.setOnClickListener { fragmentChanger.changeFragment(2) }

        return v
    }
}