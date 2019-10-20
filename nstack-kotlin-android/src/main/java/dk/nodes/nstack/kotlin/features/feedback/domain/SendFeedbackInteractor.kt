package dk.nodes.nstack.kotlin.features.feedback.domain

import dk.nodes.nstack.kotlin.features.common.domain.Interactor
import dk.nodes.nstack.kotlin.features.feedback.domain.model.Feedback

internal interface SendFeedbackInteractor : Interactor {
    var input: Input?

    data class Input(
        val feedback: Feedback
    )
}