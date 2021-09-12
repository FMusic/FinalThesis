package fm.pathfinder.fragments

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import fm.pathfinder.R
import fm.pathfinder.activities.MainActivity

class MainMenuFragment : Fragment(R.layout.fragment_main_menu) {
    lateinit var fragmentChanger: FragmentChangeListener

    companion object {
        @JvmStatic
        fun newInstance(mainActivity: MainActivity) =
            MainMenuFragment().apply {
                fragmentChanger = mainActivity
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val btnStartScan = view?.findViewById<Button>(R.id.btnStartScan)
        val btnShowData = view?.findViewById<Button>(R.id.btnOpenData)

        btnStartScan?.setOnClickListener{
            fragmentChanger.changeFragment(1)
        }
    }
}