package dk.nodes.nstack.sample

import android.app.Application
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.sample.models.Translation
import timber.log.Timber
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NStack.debugMode = true
        NStack.translationClass = Translation::class.java
        NStack.setRefreshPeriod(1, TimeUnit.MINUTES)
        NStack.init(this)

        Timber.plant(Timber.DebugTree())
    }
}