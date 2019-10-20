package dk.nodes.nstack.demo.home

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import dk.nodes.nstack.demo.BuildConfig
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.exceptions.FeedbackSendFailedException
import dk.nodes.nstack.kotlin.features.feedback.domain.model.Feedback
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeFragment : Fragment(R.layout.fragment_home), CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.Default

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yesButton.setOnClickListener { Toast.makeText(context, "YES", Toast.LENGTH_SHORT).show() }

        setupFeedbackButton(feedbackButton = feedbackWithScreenshotButton)
    }

    private fun setupFeedbackButton(feedbackButton: Button) {
        feedbackButton.setOnClickListener {
            launch {
                sendFeedbackWithScreenshot()
            }
        }
    }

    private suspend fun sendFeedbackWithScreenshot() = coroutineScope {
        val screenshot = NStack.takeScreenshot()

        delay(500)

        val feedback = Feedback(
            appVersion = BuildConfig.VERSION_NAME,
            deviceName = "BatMobile",
            name = "John Smith",
            email = "jsmith@abc.xyz",
            message = "Hello",
            image = screenshot
        )

        try {
            NStack.sendFeedback(feedback)

            showToast("Feedback sent, please check nstack.io")
        } catch (exception: FeedbackSendFailedException) {
            showToast("Error when sending feedback")
        }
    }

    private fun showToast(message: String) {
        runBlocking(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}