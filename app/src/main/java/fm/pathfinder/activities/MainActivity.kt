package fm.pathfinder.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fm.pathfinder.R
import fm.pathfinder.fragments.MainMenuFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, MainMenuFragment.newInstance())
            commit()
        }

    }
}