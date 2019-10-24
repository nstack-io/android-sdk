package dk.nodes.nstack.kotlin.features.feedback.presentation

import androidx.annotation.StringRes

internal data class FeedbackViewState(
    val isLoading: Boolean,
    val errorMessage: String?,
    val isFeedbackSent: Boolean?,
    @StringRes val title : Int
)
