package dk.nodes.nstack.demo.update

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.demo.extensions.showToast
import kotlinx.android.synthetic.main.fragment_button.*
import timber.log.Timber

class AppUpdateGoogleFragment : Fragment(R.layout.fragment_button) {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateBtn.setOnClickListener {
            startInAppUpdate(AppUpdateType.FLEXIBLE)
        }
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