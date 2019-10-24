package dk.nodes.nstack.kotlin.features.feedback.presentation

internal data class FeedbackViewState(
    val isLoading: Boolean,
    val errorMessage: String?,
    val isFeedbackSent: Boolean?
)
