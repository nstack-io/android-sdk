package dk.nodes.nstack.demo.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.nodes.nstack.demo.BuildConfig
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.exceptions.FeedbackSendFailedException
import dk.nodes.nstack.kotlin.features.feedback.domain.model.Feedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val viewStateInternal: MutableLiveData<HomeViewState> = MutableLiveData()

    val viewState: LiveData<HomeViewState> = viewStateInternal

    init {
        viewStateInternal.value = HomeViewState(
            isLoading = false,
            errorMessage = null,
            isFeedbackSent = null
        )
    }

    fun sendFeedbackWithScreenshot() {
        viewStateInternal.value = viewStateInternal.value?.copy(
            isLoading = true
        )
        viewModelScope.launch {
            val screenshot = NStack.takeScreenshot()

            delay(500)

            val feedback = Feedback(
                appVersion = BuildConfig.VERSION_NAME,
                deviceName = "BatMobile",
                name = "John Smith",
                email = "jsmith@abc.xyz",
                message = "Hello",
                image = screenshot
            )

            try {
                withContext(Dispatchers.IO) {
                    NStack.sendFeedback(feedback)
                }

                viewStateInternal.value = viewStateInternal.value?.copy(
                    isLoading = false,
                    errorMessage = null,
                    isFeedbackSent = true
                )
            } catch (exception: FeedbackSendFailedException) {
                viewStateInternal.value = viewStateInternal.value?.copy(
                    isLoading = false,
                    errorMessage = "Error when sending feedback"
                )
            }
        }
    }
}
