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
import fm.pathfinder.fragments.*
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(),
    MultiplePermissionsListener {
    private var backPressed: Boolean = false
    private var permissionsGranted = false
    private var activeFragment = -1

    private var stringToWrite: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FragmentChanger.changeFragment(supportFragmentManager, 0)
        if (!permissionsGranted)
            if (Build.VERSION.SDK_INT >= 33)
                askForPermissions()
            else
                askForOlderPermissions()
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
                val toastText = "Press back again for exit"
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
            }
        }
        if (activeFragment > 0) {
            FragmentChanger.changeFragment(supportFragmentManager, 0)
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

