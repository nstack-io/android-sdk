package dk.nodes.nstack.demo.update

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.demo.extensions.showToast
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.AppUpdate
import dk.nodes.nstack.kotlin.models.AppUpdateState
import dk.nodes.nstack.kotlin.models.Result
import dk.nodes.nstack.kotlin.models.state
import dk.nodes.nstack.kotlin.models.update
import kotlinx.android.synthetic.main.fragment_button.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Sample implementation using AppOpen only */
class AppUpdateNStackFragment : Fragment(R.layout.fragment_button) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNStackUpdate()
    }

    private fun setupNStackUpdate() {
        updateBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                checkNStackUpdate()
            }
        }
    }

    private suspend fun checkNStackUpdate() {
        when (val result = withContext(Dispatchers.IO) { NStack.appOpen() }) {
            is Result.Success -> {
                when (result.value.data.update.state) {
                    AppUpdateState.NONE -> { /* Nothing to do */
                    }
                    AppUpdateState.UPDATE -> showUpdateDialog(result.value.data.update)
                    AppUpdateState.FORCE -> showForceDialog(result.value.data.update)
                    AppUpdateState.CHANGELOG -> showChangelogDialog(result.value.data.update)
                }
            }
            is Result.Error -> {
                showToast(result.error.toString())
            }
        }
    }

    private fun showUpdateDialog(
        appUpdate: AppUpdate
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(appUpdate.update?.translate?.title ?: return)
            .setMessage(appUpdate.update?.translate?.message ?: return)
            .setPositiveButton(appUpdate.update?.translate?.positiveButton) { dialog, _ ->
                startPlayStore()
                dialog.dismiss()
            }
            .show()
    }

    private fun showForceDialog(appUpdate: AppUpdate) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(appUpdate.update?.translate?.title ?: return)
            .setMessage(appUpdate.update?.translate?.message ?: return)
            .setCancelable(false)
            .setPositiveButton(appUpdate.update?.translate?.positiveButton, null)
            .create()

        dialog.setOnShowListener {
            val b = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            b.setOnClickListener {
                startPlayStore()
            }
        }

        dialog.show()
    }

    private fun startPlayStore() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${requireActivity().packageName}")
                )
            )
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${requireActivity().packageName}")
                )
            )
        }
    }
}