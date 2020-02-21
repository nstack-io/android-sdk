package dk.nodes.nstack.kotlin.appupdate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import dk.nodes.nstack.kotlin.NStack

class InAppUpdateActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appUpdateInfo = appUpdateInfo
        if (appUpdateInfo == null) {
            finish()
            return
        } else {
            startInAppUpdate(appUpdateInfo, intent.getIntExtra(UP_STRATEGY, AppUpdateType.FLEXIBLE))
        }
    }

    private fun startInAppUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        if (appUpdateInfo.isUpdateTypeAllowed(updateType))
            NStack.appUpdateManager.startUpdateFlowForResult(
                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                appUpdateInfo,
                updateType,
                // The current activity making the update request.
                this,
                // Include a request code to later monitor this update request.
                REQ_CODE
            )
        else {
            onResult(InAppUpdateResult.Unavailable(InAppUpdateAvailability.fromInt(appUpdateInfo.updateAvailability())))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            when (resultCode) {
                RESULT_OK -> onResult(InAppUpdateResult.Success)
                RESULT_CANCELED -> onResult(InAppUpdateResult.Cancelled)
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> onResult(InAppUpdateResult.Fail)
                else -> onResult(InAppUpdateResult.Unknown)
            }
        } else {
            onResult(InAppUpdateResult.Unknown)
        }
        finish()
    }

    companion object {
        private const val REQ_CODE = 5168
        private const val UP_STRATEGY = "updateStrategy"
        private var appUpdateInfo: AppUpdateInfo? = null
        private var onResult: (InAppUpdateResult) -> Unit = {}

        fun createIntent(
            context: Context, appUpdateInfo: AppUpdateInfo,
            updateStrategy: Int,
            onResult: (InAppUpdateResult) -> Unit
        ): Intent {
            InAppUpdateActivity.onResult = onResult
            InAppUpdateActivity.appUpdateInfo = appUpdateInfo
            return Intent(context, InAppUpdateActivity::class.java)
                .putExtra(UP_STRATEGY, updateStrategy)
        }
    }
}