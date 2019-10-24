package dk.nodes.nstack.kotlin.models

typealias Base64String = String

data class Feedback(
    val appVersion: String,
    val deviceName: String,
    val name: String,
    val email: String,
    val message: String,
    val image: Base64String?
)
