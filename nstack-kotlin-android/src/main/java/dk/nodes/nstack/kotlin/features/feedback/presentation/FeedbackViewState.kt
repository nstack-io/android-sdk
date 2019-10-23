package dk.nodes.nstack.kotlin.features.feedback.presentation

data class FeedbackViewState(
    val isLoading: Boolean,
    val errorMessage: String?,
    val isFeedbackSent: Boolean?
)
