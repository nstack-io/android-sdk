package dk.nodes.nstack.demo.splash

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashTimer(
    private val duration: Long,
    private val listener: () -> Unit
) : LifecycleObserver {

    private var timeStart = System.currentTimeMillis()

    private var timerJob: Job? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        timeStart = System.currentTimeMillis()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        timerJob?.cancel()
    }

    fun finish() {
        val timeNow = System.currentTimeMillis()
        val timePassed = timeNow - timeStart
        val timeRemaining = duration - timePassed
        if (timeRemaining > 0) {
            timerJob = GlobalScope.launch {
                delay(timeRemaining)
                withContext(Dispatchers.Main) {
                    listener()
                }
            }
        } else {
            listener()
        }
    }
}