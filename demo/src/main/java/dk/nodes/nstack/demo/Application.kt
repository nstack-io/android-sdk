package dk.nodes.nstack.demo

import android.app.Application
import android.util.Log
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.enableLiveEdit

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        NStack.translationClass = Translation::class.java
        NStack.init(this)
        if (BuildConfig.DEBUG) {
            NStack.debugMode = true
            NStack.enableLiveEdit(this)
        }
        NStack.appOpen {
            Log.d("AppOpen", it.toString())
        }
    }
}