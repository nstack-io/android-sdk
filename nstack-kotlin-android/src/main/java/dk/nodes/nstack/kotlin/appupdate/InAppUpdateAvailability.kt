package dk.nodes.nstack.kotlin.appupdate

import com.google.android.play.core.install.model.UpdateAvailability

sealed class InAppUpdateAvailability {
    object DeveloperTriggeredUpdateInProgress : InAppUpdateAvailability()
    object Unknown : InAppUpdateAvailability()
    object Available : InAppUpdateAvailability()
    object NotAvailable : InAppUpdateAvailability()

    companion object {
        fun fromInt(value: Int): InAppUpdateAvailability {
            return when (value) {
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> InAppUpdateAvailability.DeveloperTriggeredUpdateInProgress

                UpdateAvailability.UPDATE_AVAILABLE -> InAppUpdateAvailability.Available

                UpdateAvailability.UPDATE_NOT_AVAILABLE -> InAppUpdateAvailability.NotAvailable
                else -> {
                    InAppUpdateAvailability.Unknown
                }
            }
        }
    }
}


