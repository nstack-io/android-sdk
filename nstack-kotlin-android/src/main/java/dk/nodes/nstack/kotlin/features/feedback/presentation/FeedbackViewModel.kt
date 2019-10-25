package dk.nodes.nstack.kotlin.features.feedback.presentation

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.features.feedback.domain.model.toImageData
import dk.nodes.nstack.kotlin.models.FeedbackType
import dk.nodes.nstack.kotlin.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class FeedbackViewModel : ViewModel() {

    private val viewStateInternal: MutableLiveData<FeedbackViewState> = MutableLiveData()

    val viewState: LiveData<FeedbackViewState> = viewStateInternal

    var feedbackImage: Bitmap? = null

    var feedbackType: FeedbackType = FeedbackType.FEEDBACK
        set(value) {
            field = value
            viewStateInternal.value = viewStateInternal.value?.copy(
                title = getTitle()
            )
        }

    init {
        viewStateInternal.value = FeedbackViewState(
            isLoading = false,
            errorMessage = null,
            isFeedbackSent = null,
            title = getTitle()
        )
    }

    fun sendFeedback(
        name: String,
        email: String,
        message: String,
        image: Bitmap?,
        type: FeedbackType
    ) {
        viewModelScope.launch {
            viewStateInternal.value = viewStateInternal.value?.copy(
                isLoading = true
            )

            when (val result = withContext(Dispatchers.IO) {
                NStack.Feedback.postFeedback(
                    name = name,
                    email = email,
                    message = message,
                    image = image?.toImageData(),
                    type = type
                )
            }) {
                is Result.Success -> {
                    viewStateInternal.value = viewStateInternal.value?.copy(
                        isLoading = false,
                        errorMessage = null,
                        isFeedbackSent = true
                    )
                }
                is Result.Error -> {
                    viewStateInternal.value = viewStateInternal.value?.copy(
                        isLoading = false,
                        errorMessage = result.error.toString()
                    )
                }
            }
        }
    }

    private fun getTitle() = when (feedbackType) {
        FeedbackType.BUG -> R.string.feedback_title_bug
        else -> R.string.feedback_title
    }
}
