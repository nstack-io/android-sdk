package dk.nodes.nstack.kotlin.features.feedback.presentation

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {

    private val viewStateInternal: MutableLiveData<FeedbackViewState> = MutableLiveData()

    val viewState: LiveData<FeedbackViewState> = viewStateInternal

    init {
        viewStateInternal.value = FeedbackViewState(
            isLoading = false,
            errorMessage = null,
            isFeedbackSent = null
        )
    }

    fun sendFeedback(name : String, email : String, message : String, image : Bitmap?) {
        viewModelScope.launch {
            viewStateInternal.value = viewStateInternal.value?.copy(
                isLoading = true
            )

            delay(3000) // TODO: NStack.Feedback.send()

            viewStateInternal.value = viewStateInternal.value?.copy(
                isFeedbackSent = true
            )
        }
    }
}
