package fm.pathfinder.collecting

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SensorsProcessor(
    mSensorManager: SensorManager,
    private val mapsFragment: MapsFragment,
    locationScanner: LocationScanner
) : SensorEventListener {
    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f
    private var timestamp: Float = 0f
    private val samplingPeriod = 10000000
    lateinit var mGravity: FloatArray
    lateinit var mGeomagnetic: FloatArray

    init {
        val linearAccelerationSensor =
            mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val rotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//        mSensorManager.registerListener(this, linearAccelerationSensor, samplingPeriod)
//        mSensorManager.registerListener(this, gravitySensor, samplingPeriod)
//        mSensorManager.registerListener(this, gyroSensor, samplingPeriod)
//        mSensorManager.registerListener(this, accelerometerSensor, samplingPeriod)
//        mSensorManager.registerListener(this, magneticSensor, samplingPeriod)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
    //            mapsFragment.logIt("LinAcc: ${event.values[0]} ${event.values[1]} ${event.values[2]}\n")
                Log.i(TAG, "LinAcc: ${event.values[0]} ${event.values[1]} ${event.values[2]}\n")
            }
            Sensor.TYPE_GRAVITY -> {
    //            mapsFragment.logIt("Grav: ${event.values[0]} ${event.values[1]} ${event.values[2]}\n")
                Log.i(TAG, "Grav: ${event.values[0]} ${event.values[1]} ${event.values[2]}\n")
            }
            Sensor.TYPE_GYROSCOPE -> {
    //            mapsFragment.logIt("Gyro: ${event.values[0]} ${event.values[1]} ${event.values[2]}\n")
                Log.i(TAG, "Gyro: ${event.values[0]} ${event.values[1]} ${event.values[2]}\n")
            }
            Sensor.TYPE_ACCELEROMETER -> mGravity = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> mGeomagnetic = event.values
        };
        if (this::mGeomagnetic.isInitialized && this::mGravity.isInitialized) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation);
                val azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                Log.i("magn", "Azimut: $azimut")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        mapsFragment.logIt("LinAcc(AccChange): new acc:${accuracy}\n")
    }


    companion object {
        private const val TAG = "Sensors"
    }
}
