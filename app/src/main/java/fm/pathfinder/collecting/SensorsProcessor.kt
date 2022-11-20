package fm.pathfinder.collecting

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
import fm.pathfinder.fragments.MapsFragment

class SensorsProcessor(
    mSensorManager: SensorManager,
    private val mapsFragment: MapsFragment,
    val locationCollector: LocationCollector
)  {
    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f
    private var timestamp: Float = 0f

    init {

    }

    fun onSensorChanged(event: SensorEvent) {
        var shouldWrite = true
        val sb = StringBuffer()
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION-> {
                sb.append("LinAcc: ")
                sb.append("${event.values[0]} ${event.values[1]} ${event.values[2]}\n")
            }
            Sensor.TYPE_ROTATION_VECTOR->{
            }
        }
        if (shouldWrite && sb.isNotBlank()) {
            val str = sb.toString()
            Log.i(TAG, str)
            mapsFragment.logIt(str)
        }

    }

//    private fun calculateAzimut(sb: StringBuffer): Boolean {
//
//    }

    fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        mapsFragment.logIt("LinAcc(AccChange): new acc:${accuracy}\n")
    }


    companion object {
        private const val TAG = "Sensors"
    }
}
