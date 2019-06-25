package dk.nodes.nstack.kotlin.managers

import android.content.Context
import android.net.ConnectivityManager

class ConnectionManager(private val context: Context) {

    val isConnected: Boolean
        get() {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnectedOrConnecting
        }
}
