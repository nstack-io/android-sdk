package dk.nodes.nstack.kotlin.features.feedback.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.models.FeedbackType
import kotlinx.android.synthetic.main.activity_feedback.*

internal class FeedbackActivity : AppCompatActivity(R.layout.activity_feedback),
    FeedbackValidator.Callback {

    companion object {
        const val EXTRA_FEEDBACK_TYPE = "feedback_type"
    }

    private lateinit var viewModel: FeedbackViewModel

    private val feedbackValidator = FeedbackValidator(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[FeedbackViewModel::class.java]
        viewModel.viewState.observe(this, Observer(this::showViewState))
        viewModel.feedbackType = getExtraType()

        nameInputView.addTextChangedListener {
            feedbackValidator.name = it.toString()
        }

        emailInputView.addTextChangedListener {
            feedbackValidator.email = it.toString()
        }

        messageInputView.addTextChangedListener {
            feedbackValidator.message = it.toString()
        }

        submitButton.setOnClickListener {
            viewModel.sendFeedback(
                name = feedbackValidator.name,
                email = feedbackValidator.email,
                message = feedbackValidator.message,
                type = viewModel.feedbackType
            )
        }
    }

    override fun onValidationUpdate(isValid: Boolean) {
        submitButton.isEnabled = isValid
    }

    private fun showViewState(state: FeedbackViewState) {
        loadingView.isVisible = state.isLoading
        contentView.isVisible = !state.isLoading

        if (state.isFeedbackSent == true) {
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
            finish()
        }

        state.errorMessage?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        toolbar.setTitle(state.title)
    }

    private fun getExtraType() = FeedbackType.fromSlug(intent?.extras?.getString(EXTRA_FEEDBACK_TYPE))
}
