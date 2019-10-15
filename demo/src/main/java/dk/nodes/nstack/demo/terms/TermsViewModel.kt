package dk.nodes.nstack.demo.terms

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.nodes.nstack.kotlin.NStack

class TermsViewModel : ViewModel() {

    val viewState: MutableLiveData<TermsViewState> = MutableLiveData()

    init {
        viewState.value = TermsViewState(
                isLoading = false,
                errorMessage = null,
                termsContent = null,
                isAccepted = false
        )
    }

    fun loadTerms(termsID: Long) {
        NStack.Terms.getLatestTerms(
                termsID = termsID,
                onSuccess = {
                    viewState.value = viewState.value?.copy(
                            isLoading = false,
                            errorMessage = null,
                            termsContent = it.content.data,
                            isAccepted = it.hasViewed
                    )
                },
                onError = {
                    viewState.value = viewState.value?.copy(
                            isLoading = false,
                            errorMessage = it.localizedMessage
                    )
                }
        )
    }

    fun acceptTerms() {
        NStack.Terms.acceptTerms(
                versionID = 4,
                userID = "unknown",
                onSuccess = {
                    viewState.value = viewState.value?.copy(
                            isLoading = false,
                            errorMessage = null,
                            isAccepted = true
                    )
                },
                onError = {
                    viewState.value = viewState.value?.copy(
                            isLoading = false,
                            errorMessage = it.localizedMessage
                    )
                }
        )
    }
}