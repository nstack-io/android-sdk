package dk.nodes.nstack.kotlin.models

data class Feedback(
    val appVersion: String,
    val deviceName: String,
    val name: String,
    val email: String,
    val message: String
)
