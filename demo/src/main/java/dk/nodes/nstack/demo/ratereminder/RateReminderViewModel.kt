package dk.nodes.nstack.demo.ratereminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.nodes.nstack.demo.RateReminderActions
import dk.nodes.nstack.kotlin.NStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RateReminderViewModel : ViewModel() {

    private val viewStateInternal: MutableLiveData<RateReminderViewState> = MutableLiveData()
    val viewState: LiveData<RateReminderViewState> = viewStateInternal

    init {
        viewStateInternal.value = RateReminderViewState(
            isLoading = false,
            shouldShowReminder = false
        )
    }

    fun checkRateReminder() {
        viewModelScope.launch {
            val shouldShow = withContext(Dispatchers.IO) {
                NStack.RateReminder.shouldShow()
            }
            viewStateInternal.value = viewStateInternal.value?.copy(
                    shouldShowReminder = shouldShow
            )
        }
    }

    fun runFirstAction() {
        viewModelScope.launch(Dispatchers.IO) {
            RateReminderActions.firstaction()
        }
    }

    fun runSecondAction() {
        viewModelScope.launch(Dispatchers.IO) {
            RateReminderActions.secondaction()
        }
    }
}