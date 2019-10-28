package dk.nodes.nstack.demo.ratereminder

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.demo.Translation
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.RateReminderAnswer
import kotlinx.android.synthetic.main.fragment_ratereminder.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RateReminderFragment : Fragment(R.layout.fragment_ratereminder) {

    private lateinit var viewModel: RateReminderViewModel

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[RateReminderViewModel::class.java]
        viewModel.viewState.observe(this, Observer(this::showViewState))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstActionButton.setOnClickListener {
            viewModel.runFirstAction()
        }

        secondActionButton.setOnClickListener {
            viewModel.runSecondAction()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkRateReminder()
    }

    private fun showViewState(rateReminderViewState: RateReminderViewState) {
        when {
            rateReminderViewState.shouldShowReminder -> {
                scope.launch {
                    NStack.RateReminder.apply {
                        title = Translation.rateReminder.rateReminderTitle
                        message = Translation.rateReminder.rateReminderMessage
                        yesButton = Translation.defaultSection.yes
                        noButton = Translation.defaultSection.no
                        skipButton = Translation.defaultSection.later
                    }
                    when (NStack.RateReminder.show(requireContext())) {
                        RateReminderAnswer.POSITIVE -> {
                            showToast("TODO: Go to PlayStore")
                        }
                        RateReminderAnswer.NEGATIVE -> {
                            showToast("TODO: Go to feedback screen")
                        }
                        RateReminderAnswer.SKIP -> {
                            showToast("TODO: Skipped")
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}