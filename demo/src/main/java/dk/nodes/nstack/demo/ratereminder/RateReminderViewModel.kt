package dk.nodes.nstack.demo.ratereminder

import androidx.lifecycle.*
import dk.nodes.nstack.demo.RateReminderActions
import dk.nodes.nstack.demo.terms.TermsViewState
import dk.nodes.nstack.kotlin.NStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RateReminderViewModel : ViewModel() {

    private val viewStateInternal: MutableLiveData<RateReminderViewState> = MutableLiveData()
    val viewState: LiveData<RateReminderViewState> = viewStateInternal

    init {
        viewStateInternal.value = RateReminderViewState(isLoading = false, shouldShowReminder = false )
    }

    fun checkRateReminder() {
        viewModelScope.launch {
            val shouldShow = withContext(Dispatchers.IO) { NStack.RateReminder.shouldShow() }
            viewStateInternal.value = viewStateInternal.value?.copy(
                    shouldShowReminder = shouldShow
            )
        }
    }

    fun someRateMethod() {
        viewModelScope.launch(Dispatchers.IO) {
            RateReminderActions.rideCompleted()
        }
    }

    fun someOtherRateMethod() {
        viewModelScope.launch(Dispatchers.IO) {
            //RateReminderActions.rideCompleted()
        }
    }

}