package dk.nodes.nstack.demo.update

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.demo.extensions.showToast
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.appupdate.InAppUpdateStrategy
import kotlinx.android.synthetic.main.fragment_button.*
import kotlinx.coroutines.launch
/**
* Sample implementation using automatic update */
class AppUpdateAutoFragment : Fragment(R.layout.fragment_button) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAutomatic()
    }

    private fun setupAutomatic() {
        updateBtn.setOnClickListener {
            ProcessLifecycleOwner.get().lifecycleScope.launch {
                val result = NStack.updateApp(InAppUpdateStrategy.Flexible)
                showToast(result.toString())
            }
        }
    }
}