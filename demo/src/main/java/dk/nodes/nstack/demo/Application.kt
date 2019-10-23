package dk.nodes.nstack.demo

import android.app.Application
import android.util.Log
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.Result
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
                when (val result = NStack.appOpen()) {
                    is Result.Success ->  Log.d("AppOpenResult: Success", result.toString())
                    is Result.Error -> Log.d("AppOpenResult: Error", result.toString())
                }
            }
        }
    }
}
