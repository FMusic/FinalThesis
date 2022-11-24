package fm.pathfinder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import fm.pathfinder.R
import fm.pathfinder.MainActivity

class MainMenuFragment : Fragment(R.layout.fragment_main_menu) {

    companion object {
        @JvmStatic
        fun newInstance() =
            MainMenuFragment().apply {
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_main_menu, container, false)
        val btnStartScan = v.findViewById<Button>(R.id.btnOpenMap)
        val btnOpenData = v.findViewById<Button>(R.id.btnOpenData)

        btnStartScan.setOnClickListener {
            FragmentChanger.changeFragment(parentFragmentManager, 1)
        }
        btnOpenData.setOnClickListener { FragmentChanger.changeFragment(parentFragmentManager, 2) }

        return v
    }
}