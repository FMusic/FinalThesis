package fm.pathfinder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import fm.pathfinder.ui.MainMenuFragment
import fm.pathfinder.ui.MainMenuPresenter

class MainActivity : AppCompatActivity(),
    MultiplePermissionsListener {
    private var backPressed: Boolean = false
    private var permissionsGranted = false
    private var activeFragment = -1
    private var stringToWrite: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("MainActivity", "onCreate")

        setupBackButton()

        if (Build.VERSION.SDK_INT >= 33) {
            askForPermissions()
        } else {
            askForOlderPermissions()
        }
    }

    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is MainMenuFragment) {
                    if (backPressed) finishAffinity()
                    else {
                        backPressed = true
                        Toast.makeText(applicationContext, "Press back again for exit", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    MainMenuPresenter.changeFragment(supportFragmentManager, 0)
                }
            }
        })
    }

    private fun askForOlderPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
            )
            .withListener(this)
            .check()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun askForPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.NEARBY_WIFI_DEVICES,
                android.Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
            )
            .withListener(this)
            .check()
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        if (report != null && report.areAllPermissionsGranted()) {
            Log.i("MainActivity", "All permissions granted.")
            MainMenuPresenter.changeFragment(supportFragmentManager, 0)
        } else {
            Toast.makeText(
                this,
                "Permissions not granted. App functionality may be limited.",
                Toast.LENGTH_LONG
            ).show()
//            finish() // optional: close the app if permissions are mandatory
        }
    }

    override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?,
        token: PermissionToken?
    ) {
        token?.continuePermissionRequest() // Let Dexter continue prompting
    }

    @Deprecated("Handled via Storage Access Framework")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

//        if (requestCode == Constants.CREATE_FILE && resultCode == Activity.RESULT_OK) {
//            data?.data?.also { uri ->
//                contentResolver.openFileDescriptor(uri, "w")?.use { fd ->
//                    FileOutputStream(fd.fileDescriptor).use { stream ->
//                        stream.write(stringToWrite?.toByteArray() ?: ByteArray(0))
//                        stringToWrite = null
//                    }
//                }
//            }
//        }
    }
}

