package fm.pathfinder.collecting.sensors

import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.util.Log
import fm.pathfinder.collecting.MapsFragment

class WifiRangeCallback(private val mapsFragment: MapsFragment) : RangingResultCallback() {
        override fun onRangingFailure(code: Int) {
            Log.e("rtt", "code of ranging failure: $code")
        }

        override fun onRangingResults(results: MutableList<RangingResult>) {
            results.forEach {
// filtering results: https://developer.android.com/reference/android/net/wifi/rtt/RangingResult#constants_1
                if (it.status != 0) {
                    Log.i("rtt", "status of failing rtt result: ${it.status}")
                    return
                }
                Log.i("rtt", it.toString())
                mapsFragment.logRtt(it)
            }
        }


}
