package dk.nodes.nstack.demo

import android.app.Application
import android.util.Log
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.managers.LiveEditManager

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        NStack.translationClass = Translation::class.java
        // NStack.liveEditEnabled = true
        if (BuildConfig.DEBUG) {
            NStack.debugMode = true
            LiveEditManager()
        }
        NStack.init(this)
        NStack.appOpen {
            Log.d("AppOpen", it.toString())
        }
    }
}