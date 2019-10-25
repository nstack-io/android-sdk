package dk.nodes.nstack.demo.ratereminder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.kotlin.NStack
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

        someActionButton.setOnClickListener {
            viewModel.someRateMethod()
        }

        someOtherActionButton.setOnClickListener {
            viewModel.someOtherRateMethod()
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
                    val result = NStack.RateReminder.show(requireContext())
                }
            }
        }
    }
}