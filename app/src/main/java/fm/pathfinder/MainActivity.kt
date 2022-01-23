package fm.pathfinder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import fm.pathfinder.fragments.DataStorageFragment
import fm.pathfinder.fragments.FragmentChangeListener
import fm.pathfinder.fragments.MainMenuFragment
import fm.pathfinder.fragments.MapsFragment

class MainActivity : AppCompatActivity(), FragmentChangeListener,
    MultiplePermissionsListener {
    private var backPressed: Boolean = false
    private var permissionsGranted = false
    private var activeFragment = -1

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
        activeFragment = id
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
                    replace(R.id.fragment_container, DataStorageFragment.newInstance())
                }
            }
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            commit()
        }
        backPressed = false
    }

    private fun askForPermissions() {
        val dexter = Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE
            )
            .withListener(this)
        dexter.check()
    }

    override fun onBackPressed() {
        if (activeFragment == 0) {
            if (backPressed)
                finishAffinity()
            else {
                backPressed = true
                Toast.makeText(this, "Press back again for exit", Toast.LENGTH_SHORT).show()
            }
        }
        if (activeFragment > 0) {
            changeFragment(0)
        }
    }

    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
        permissionsGranted = true
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
        TODO("Not yet implemented")
    }
}

