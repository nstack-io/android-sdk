package dk.nodes.nstack.demo.terms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.nodes.nstack.kotlin.NStack

class TermsViewModel : ViewModel() {

    private var termVersionID = -1L

    private val viewStateInternal: MutableLiveData<TermsViewState> = MutableLiveData()

    val viewState : LiveData<TermsViewState> = viewStateInternal

    init {
        viewStateInternal.value = TermsViewState(
                isLoading = false,
                errorMessage = null,
                termsName = null,
                termsContent = null,
                isAccepted = null
        )
    }

    fun loadTerms(termsID: Long) {
        viewStateInternal.value = viewStateInternal.value?.copy(
                isLoading = true
        )
        NStack.Terms.getTermsDetails(
                termsID = termsID,
                onSuccess = {
                    termVersionID = it.versionID
                    viewStateInternal.value = viewStateInternal.value?.copy(
                            isLoading = false,
                            errorMessage = null,
                            termsName = it.versionName,
                            termsContent = it.content.data,
                            isAccepted = it.hasViewed
                    )
                },
                onError = {
                    viewStateInternal.value = viewStateInternal.value?.copy(
                            isLoading = false,
                            errorMessage = it.localizedMessage
                    )
                }
        )
    }

    fun acceptTerms() {
        viewStateInternal.value = viewStateInternal.value?.copy(
                isLoading = true
        )
        NStack.Terms.setTermsViewed(
                versionID = termVersionID,
                userID = "unknown",
                onSuccess = {
                    viewStateInternal.value = viewStateInternal.value?.copy(
                            isLoading = false,
                            errorMessage = null,
                            isAccepted = true
                    )
                },
                onError = {
                    viewStateInternal.value = viewStateInternal.value?.copy(
                            isLoading = false,
                            errorMessage = it.localizedMessage
                    )
                }
        )
    }
}