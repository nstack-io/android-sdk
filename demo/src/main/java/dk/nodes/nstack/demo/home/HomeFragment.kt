package dk.nodes.nstack.demo.home

import androidx.fragment.app.Fragment
import dk.nodes.nstack.demo.R

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