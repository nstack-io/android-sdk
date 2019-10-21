package dk.nodes.nstack.demo

import android.app.Application
import android.util.Log
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.AppOpenResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        NStack.baseUrl = "https://nstack-staging.vapor.cloud"
        NStack.translationClass = Translation::class.java
        NStack.init(this, BuildConfig.DEBUG)
        if (BuildConfig.DEBUG) {
            NStack.enableMenuOnShake(this)
        }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                when (val result: AppOpenResult = NStack.appOpen()) {
                    is AppOpenResult.Success -> {
                        Log.d("AppOpenResult", result.toString())
                    }
                    is AppOpenResult.Failure -> {
                        Log.d("AppOpenResult", "Failure")
                    }
                    is AppOpenResult.NoInternet -> {
                        Log.d("AppOpenResult", "NoInternet")
                    }
                }
            }
        }
    }
}
