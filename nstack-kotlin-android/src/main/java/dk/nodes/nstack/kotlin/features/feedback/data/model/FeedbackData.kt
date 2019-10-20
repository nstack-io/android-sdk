package dk.nodes.nstack.kotlin.features.feedback.data.model

import dk.nodes.nstack.kotlin.models.Base64String

internal open class FeedbackData(
    val appVersion: String,
    val deviceName: String,
    val name: String,
    val email: String,
    val message: String,
    val image: Base64String?
)

