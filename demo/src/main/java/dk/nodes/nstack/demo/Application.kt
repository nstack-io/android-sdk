package dk.nodes.nstack.demo

import android.app.Application
import dk.nodes.nstack.kotlin.NStack

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        NStack.baseUrl = "https://nstack.io"
        NStack.translationClass = Translation::class.java
        NStack.init(this, BuildConfig.DEBUG)
        if (BuildConfig.DEBUG) {
            NStack.enableMenuOnShake(this)
        }
    }
}
