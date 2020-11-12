package dk.nodes.nstack.demo

import android.app.Application
import dk.nodes.nstack.kotlin.NStack

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        NStack.baseUrl = "https://nstack-staging.vapor.cloud"
        if (BuildConfig.DEBUG) {
            NStack.enableMenuOnShake(this)
        }
    }
}
