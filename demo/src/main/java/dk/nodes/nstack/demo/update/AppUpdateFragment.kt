package dk.nodes.nstack.demo.update

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
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
import kotlinx.android.synthetic.main.fragment_app_update.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppUpdateFragment : Fragment(R.layout.fragment_app_update) {

    private val listener = { state: InstallState ->
        showToast("InstallState: $state")
    }

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNStackUpdate()
        setupPlaystoreUpdate()
        setupIntegratedUpdate()

        // Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener)
    }

    private fun setupIntegratedUpdate() {
        integratedUpdateBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                checkNStackUpdate(true)
            }
        }
    }

    private fun setupNStackUpdate() {
        nstackUpdateBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                checkNStackUpdate(false)
            }
        }
    }

    private fun setupPlaystoreUpdate() {
        playstoreUpdateBtn.setOnClickListener {
            // Returns an intent object that you use to check for an update.
            startUpdatePlaystoreFlow(AppUpdateType.IMMEDIATE)
        }
    }

    private fun startUpdatePlaystoreFlow(updateType: Int) {
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
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                // For a flexible update, use AppUpdateType.FLEXIBLE
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    requireActivity(),
                    // Include a request code to later monitor this update request.
                    AppUpdateRequestCode
                )
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            Timber.e(it)
            showToast(it.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // When status updates are no longer needed, unregister the listener.
        appUpdateManager.unregisterListener(listener)
    }

    private suspend fun checkNStackUpdate(integrate: Boolean) {
        when (val result = withContext(Dispatchers.IO) { NStack.appOpen() }) {
            is Result.Success -> {
                when (result.value.data.update.state) {
                    AppUpdateState.NONE -> { /* Nothing to do */
                    }
                    AppUpdateState.UPDATE -> showUpdateDialog(result.value.data.update, integrate)
                    AppUpdateState.FORCE -> showForceDialog(result.value.data.update, integrate)
                    AppUpdateState.CHANGELOG -> showChangelogDialog(result.value.data.update)
                }
            }
            is Result.Error -> {
                showToast(result.error.toString())
            }
        }
    }

    private fun showUpdateDialog(
        appUpdate: AppUpdate, integrate: Boolean
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(appUpdate.update?.translate?.title ?: return)
            .setMessage(appUpdate.update?.translate?.message ?: return)
            .setPositiveButton(appUpdate.update?.translate?.positiveButton) { dialog, _ ->
                if (integrate) {
                    startUpdatePlaystoreFlow(AppUpdateType.FLEXIBLE)
                } else {
                    startPlayStore()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showChangelogDialog(appUpdate: AppUpdate) {
        AlertDialog.Builder(requireContext())
            .setTitle(appUpdate.update?.translate?.title ?: return)
            .setMessage(appUpdate.update?.translate?.message ?: return)
            .setNegativeButton(appUpdate.update?.translate?.negativeButton ?: return) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showForceDialog(
        appUpdate: AppUpdate,
        integrate: Boolean
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
                if (integrate) {
                    startUpdatePlaystoreFlow(AppUpdateType.IMMEDIATE)
                } else {
                    startPlayStore()
                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppUpdateRequestCode) {
            if (resultCode != RESULT_OK) {
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