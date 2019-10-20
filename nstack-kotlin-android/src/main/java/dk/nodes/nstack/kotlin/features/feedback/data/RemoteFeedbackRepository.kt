package dk.nodes.nstack.kotlin.features.feedback.data

import dk.nodes.nstack.kotlin.features.feedback.data.model.FeedbackData
import dk.nodes.nstack.kotlin.managers.NetworkManager

private typealias CoreFeedbackData = dk.nodes.nstack.kotlin.models.Feedback

internal class RemoteFeedbackRepository(
    private val networkManager: NetworkManager
) : FeedbackRepository {

    override suspend fun send(feedbackData: FeedbackData) {
        networkManager.postFeedback(
            feedback = CoreFeedbackData(
                appVersion = feedbackData.appVersion,
                deviceName = feedbackData.deviceName,
                name = feedbackData.name,
                email = feedbackData.email,
                message = feedbackData.message,
                image = feedbackData.image
            )
        )
    }
}