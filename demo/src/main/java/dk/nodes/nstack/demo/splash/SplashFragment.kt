package dk.nodes.nstack.demo.splash

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DURATION = 1500L

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private var appOpenJob : Job? = null

    private val splashTimer = SplashTimer(DURATION) {
        findNavController().navigate(R.id.mainFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(splashTimer)
    }

    override fun onResume() {
        super.onResume()

        appOpenJob = GlobalScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = NStack.appOpen()) {
                    is Result.Success -> Log.d("AppOpenResult: Success", result.toString())
                    is Result.Error -> Log.d("AppOpenResult: Error", result.toString())
                }
                withContext(Dispatchers.Main) {
                    splashTimer.finish()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        appOpenJob?.cancel()
    }
}