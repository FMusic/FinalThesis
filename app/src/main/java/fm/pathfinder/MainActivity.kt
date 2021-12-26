package fm.pathfinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import fm.pathfinder.fragments.FragmentChangeListener
import fm.pathfinder.fragments.MainMenuFragment
import fm.pathfinder.fragments.MapsFragment

class MainActivity : AppCompatActivity(), FragmentChangeListener, PermissionListener {
    private var permissionsGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        changeFragment(0)
        if (!permissionsGranted)
            askForPermissions()
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
                0 -> {
                    replace(
                        R.id.fragment_container,
                        MainMenuFragment.newInstance(this@MainActivity)
                    )
                }
                1 -> {
                    replace(R.id.fragment_container, MapsFragment.newInstance())
                }
                2 -> {
                    replace(R.id.fragment_container, MapsFragment.newInstance())
                }
            }
            commit()
        }
    }

    private fun askForPermissions() {
        val dexter = Dexter.withContext(this)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)
        dexter.check()
    }

    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        permissionsGranted = true
    }

    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
        permissionsGranted = false
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
        TODO("Not yet implemented")
    }
}

