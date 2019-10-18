package dk.nodes.nstack.demo.terms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TermsViewModel : ViewModel() {

    private var termVersionID = -1L

    private val viewStateInternal: MutableLiveData<TermsViewState> = MutableLiveData()

    val viewState: LiveData<TermsViewState> = viewStateInternal

    init {
        viewStateInternal.value = TermsViewState(
                isLoading = false,
                errorMessage = null,
                termsName = null,
                termsContent = null,
                isAccepted = null
        )
    }

    fun loadTerms(termsID: Long) = viewModelScope.launch {
        viewStateInternal.value = viewStateInternal.value?.copy(
                isLoading = true
        )
        when (val result = withContext(Dispatchers.IO) {
            NStack.Terms.getTermsDetails(
                    termsID = termsID
            )
        }) {
            is Result.Success -> {
                termVersionID = result.value.versionID
                viewStateInternal.value = viewStateInternal.value?.copy(
                        isLoading = false,
                        errorMessage = null,
                        termsName = result.value.versionName,
                        termsContent = result.value.content.data,
                        isAccepted = result.value.hasViewed
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

    fun acceptTerms() = viewModelScope.launch {
        viewStateInternal.value = viewStateInternal.value?.copy(
                isLoading = true
        )
        when (val result = withContext(Dispatchers.IO) {
            NStack.Terms.setTermsViewed(
                    versionID = termVersionID,
                    userID = "unknown"
            )
        }) {
            is Result.Success -> {
                viewStateInternal.value = viewStateInternal.value?.copy(
                        isLoading = false,
                        errorMessage = null,
                        isAccepted = true
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