package dk.nodes.nstack.kotlin.features.feedback.domain.model


data class Feedback(
    val appVersion: String,
    val deviceName: String,
    val name: String,
    val email: String,
    val message: String,
    val image: ImageData?
)
