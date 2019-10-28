package dk.nodes.nstack.demo.splash

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dk.nodes.nstack.demo.R

private const val DURATION = 1500L

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private lateinit var viewModel: SplashViewModel

    private val splashTimer = SplashTimer(DURATION) {
        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(splashTimer)

        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        viewModel.viewState.observe(this, Observer(this::showViewState))
    }

    private fun showViewState(state: SplashViewState) {
        if (state.isFinished) {
            splashTimer.finish()
        }
    }
}