package dk.nodes.nstack.demo.update

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
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
import timber.log.Timber

/**
 * Sample implementation using AppOpen + In app update
 * */
class AppUpdateIntegratedFragment : Fragment(R.layout.fragment_button) {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupIntegratedUpdate()
    }

    private fun setupIntegratedUpdate() {
        updateBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                checkNStackUpdate(true)
            }
        }
    }

    private suspend fun checkNStackUpdate(integrate: Boolean) {
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
                startInAppUpdate(AppUpdateType.FLEXIBLE)
                dialog.dismiss()
            }
            .show()
    }

    private fun showForceDialog(
        appUpdate: AppUpdate
    ) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(appUpdate.update?.translate?.title ?: return)
            .setMessage(appUpdate.update?.translate?.message ?: return)
            .setCancelable(false)
            .setPositiveButton(appUpdate.update?.translate?.positiveButton, null)
            .create()

        dialog.setOnShowListener {
            val b = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            b.setOnClickListener {
                startInAppUpdate(AppUpdateType.IMMEDIATE)
            }
        }

        dialog.show()
    }

    private fun startInAppUpdate(updateType: Int) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    showToast("Update in progress")
                }
                UpdateAvailability.UNKNOWN -> {
                    showToast("Update availability unknown")
                }
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    if (appUpdateInfo.isUpdateTypeAllowed(updateType))
                        appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            updateType,
                            // The current activity making the update request.
                            requireActivity(),
                            // Include a request code to later monitor this update request.
                            AppUpdateRequestCode
                        )
                }
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    showToast("App Update not available")
                }
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            Timber.e(it)
            showToast(it.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppUpdateRequestCode) {
            if (resultCode != Activity.RESULT_OK) {
                showToast("Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    companion object {
        private const val AppUpdateRequestCode = 16816
    }
}
