package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.util.Constants
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.Preferences
import dk.nodes.nstack.kotlin.util.extensions.formatted
import dk.nodes.nstack.kotlin.util.extensions.iso8601Date
import java.util.Date
import java.util.UUID

internal class AppOpenSettingsManager(
    private val clientAppInfo: ClientAppInfo,
    private val preferences: Preferences,
    var versionUpdateTestMode: Boolean = false
) {

    fun getAppOpenSettings(): AppOpenSettings {
        return AppOpenSettings(
                platform = "android",
                device = clientAppInfo.deviceName,
                guid = appUUID,
                version = clientAppInfo.versionName,
                oldVersion = appOldVersion ?: clientAppInfo.versionName,
                lastUpdated = appUpdateDate,
                versionUpdateTestMode = versionUpdateTestMode,
                osVersion = clientAppInfo.osVersion
        )
    }

    fun setUpdateDate() {
        val version = clientAppInfo.versionName
        val updateDate = Date().formatted

        preferences.saveString(Constants.spk_nstack_last_updated, updateDate)
        preferences.saveString(Constants.spk_nstack_old_version, version)
    }

    private val appUUID: String
        get() {
            NLog.d(this, "Getting UUID")

            var uuid = preferences.loadString(Constants.spk_nstack_guid)

            if (uuid.isEmpty()) {
                NLog.d(this, "UUID missing -> Generating!")
                uuid = UUID.randomUUID().toString()
                preferences.saveString(Constants.spk_nstack_guid, uuid)
            }

            return uuid
        }

    private val appOldVersion: String?
        get() {
            return preferences.loadString(Constants.spk_nstack_old_version)
                .takeUnless { it.isEmpty() }
        }

    private val appUpdateDate: Date
        get() {
            return preferences.loadString(Constants.spk_nstack_last_updated).iso8601Date
        }
}
