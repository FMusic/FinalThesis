package fm.pathfinder.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fm.pathfinder.R
import fm.pathfinder.fragments.FragmentChangeListener
import fm.pathfinder.fragments.MainMenuFragment
import fm.pathfinder.fragments.MapsFragment

class MainActivity : AppCompatActivity(), FragmentChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        changeFragment(0)
    }

    /**
     * @param id - number of fragment to put in fragment_container
     * 0 - Main Menu Fragment
     * 1 - Maps Fragment
     * 2 - todo: data loader fragment, load list of scanned buildings from api and show them, on click show
     */
    override fun changeFragment(id: Int) {
        supportFragmentManager.beginTransaction().apply {
            when (id) {
                0 -> { replace(R.id.fragment_container, MainMenuFragment.newInstance(this@MainActivity)) }
                1 -> { replace(R.id.fragment_container, MapsFragment.newInstance()) }
                2 -> { replace(R.id.fragment_container, MapsFragment.newInstance()) }
            }
            commit()
        }
    }
}