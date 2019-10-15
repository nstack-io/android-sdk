package dk.nodes.nstack.demo

import android.app.Application
import android.util.Log
import dk.nodes.nstack.kotlin.NStack

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        NStack.baseUrl = "https://nstack-staging.vapor.cloud"
        NStack.translationClass = Translation::class.java
        NStack.init(this, BuildConfig.DEBUG)
        if (BuildConfig.DEBUG) {
            NStack.enableLiveEdit(this)
        }
        NStack.appOpen {
            Log.d("AppOpen", it.toString())

            val terms = NStack.Terms.getAppOpenTerms()

            Log.d("AppOpenTerms", terms.toString())
        }


        //NStack.Terms.getLatestTerms(1)
        // NStack.Terms.getTerms(1)

        NStack.Feedback.appVersion
    }
}