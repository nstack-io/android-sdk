package dk.nodes.nstack.demo.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dk.nodes.nstack.demo.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]
        viewModel.viewState.observe(this, Observer(this::showViewState))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFeedbackButton(feedbackButton = feedbackWithScreenshotButton)
    }

    private fun setupFeedbackButton(feedbackButton: Button) {
        feedbackButton.setOnClickListener {
            viewModel.sendFeedbackWithScreenshot()
        }
    }

    private fun showViewState(state: HomeViewState) {
        feedbackWithScreenshotButton.isEnabled = !state.isLoading

        if (state.isFeedbackSent == true) {
            showToast("Feedback sent, please check nstack.io")
        }

        state.errorMessage?.let {
            showToast(it)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}