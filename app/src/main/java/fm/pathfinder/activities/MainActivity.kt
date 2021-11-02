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
        val mainMenuFragment = MainMenuFragment.newInstance(this@MainActivity)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, mainMenuFragment)
            commit()
        }
    }

    override fun changeFragment(id: Int) {
        supportFragmentManager.beginTransaction().apply {
            when (id) {
                1 -> { replace(R.id.fragment_container, MapsFragment()) }
                2 -> { replace(R.id.fragment_container, MapsFragment()) }
            }
            commit()
        }
    }
}