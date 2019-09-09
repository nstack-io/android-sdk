package dk.nodes.nstack.kotlin.models

/**
 * Info for rate reminder dialog
 */
data class RateReminder(
    val title: String,
    val body: String,
    val yesButton: String,
    val laterButton: String,
    val noButton: String,
    val link: String
)
