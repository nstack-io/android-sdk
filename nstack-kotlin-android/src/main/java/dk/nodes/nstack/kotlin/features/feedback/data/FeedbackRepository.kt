package dk.nodes.nstack.kotlin.features.feedback.data

import dk.nodes.nstack.kotlin.features.feedback.data.model.FeedbackData

internal interface FeedbackRepository {
    suspend fun send(feedbackData: FeedbackData)
}