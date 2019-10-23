package dk.nodes.nstack.kotlin.features.feedback.presentation

import android.util.Patterns

class FeedbackValidator(private val callback: Callback) {

    interface Callback {
        fun onValidationUpdate(isValid: Boolean)
    }

    var name: String = ""
        set(value) {
            field = value
            validate()
        }

    var email: String = ""
        set(value) {
            field = value
            validate()
        }

    var message: String = ""
        set(value) {
            field = value
            validate()
        }

    private fun validate() {
        val hasValidName = name.isNotEmpty()
        val hasValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val hasValidMessage = message.isNotEmpty()

        callback.onValidationUpdate(hasValidName && hasValidEmail && hasValidMessage)
    }
}