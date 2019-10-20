package dk.nodes.nstack.kotlin.features.feedback.domain

import dk.nodes.nstack.kotlin.features.feedback.data.FeedbackRepository
import dk.nodes.nstack.kotlin.features.feedback.data.RemoteFeedbackRepository
import dk.nodes.nstack.kotlin.features.feedback.data.model.FeedbackData
import dk.nodes.nstack.kotlin.managers.NetworkManager

internal class RemoteSendFeedbackInteractor(
    networkManager: NetworkManager
) : SendFeedbackInteractor {

    override var input: SendFeedbackInteractor.Input? = null
    private val feedbackRepository: FeedbackRepository = RemoteFeedbackRepository(networkManager)

    override suspend fun run() {
        val feedback = this
            .input
            ?.feedback
            ?: throw IllegalStateException("run() called without input")

        feedbackRepository.send(
            with(feedback) {
                FeedbackData(
                    appVersion = appVersion,
                    deviceName = deviceName,
                    name = name,
                    email = email,
                    message = message,
                    image = image?.asJpegBase64String()
                )
            }
        )
    }
}