package fm.pathfinder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import fm.pathfinder.collecting.MapsFragment
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), FragmentChangeListener,
    MultiplePermissionsListener {
    private var backPressed: Boolean = false
    private var permissionsGranted = false
    private var activeFragment = -1

    private var stringToWrite: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        changeFragment(0)
        if (!permissionsGranted)
            if (Build.VERSION.SDK_INT >= 33)
                askForPermissions()
            else
                askForOlderPermissions()
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
                    replace(R.id.fragment_container, MapsFragment.newInstance(this@MainActivity))
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

    private fun askForOlderPermissions() {
        val dexter = Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            .withListener(this)
        dexter.check()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForPermissions() {
        val dexter = Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.NEARBY_WIFI_DEVICES,
            )
            .withListener(this)
        dexter.check()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (activeFragment == 0) {
            if (backPressed)
                finishAffinity()
            else {
                backPressed = true
                Toast.makeText(
                    this,
                    "Press back again for exit",
                    Toast.LENGTH_SHORT
                ).show()
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


    /**
     * opens an activity for saving file.
     * @param inputString required, string that will be written into file
     * @param pickerInitialUri optional, specify a URI for dir that should be
     * opened in the system file picker before app creates the document
     */
    fun createFile(inputString: String, pickerInitialUri: Uri? = null) {
        stringToWrite = inputString
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "pathfinder.json")
            pickerInitialUri?.let {
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    pickerInitialUri
                )
            }
        }
        startActivityForResult(intent, Constants.CREATE_FILE)
    }

    @Deprecated("todo in future")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.CREATE_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                contentResolver.openFileDescriptor(uri, "w")?.use { it ->
                    FileOutputStream(it.fileDescriptor).use { it2 ->
                        if (stringToWrite != null) {
                            it2.write(stringToWrite!!.toByteArray())
                        } else {
                            Toast.makeText(this, "String to write is empty", Toast.LENGTH_LONG)
                                .show()
                        }
                        stringToWrite = null
                    }
                }
            }
        }
    }
}

