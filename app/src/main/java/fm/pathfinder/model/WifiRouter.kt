package fm.pathfinder.model

import kotlin.reflect.typeOf

class WifiRouter(val ssid: String,
                 val bssid: String) {

    override fun equals(other: Any?): Boolean {
        if (other is WifiRouter){
            val oth = other as WifiRouter
            return oth.ssid == this.ssid && oth.bssid == this.bssid
        }
        return super.equals(other)
    }

}
