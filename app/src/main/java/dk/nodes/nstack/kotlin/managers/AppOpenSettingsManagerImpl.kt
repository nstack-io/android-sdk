package dk.nodes.nstack.kotlin.managers

import android.content.Context
import dk.nodes.nstack.kotlin.util.Constants
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.Preferences
import dk.nodes.nstack.kotlin.util.extensions.formatted
import dk.nodes.nstack.kotlin.util.extensions.iso8601Date
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import java.util.Date
import java.util.UUID

/**
 * Manages app open settings
 */
class AppOpenSettingsManagerImpl(
    private val context: Context,
    private val preferences: Preferences
) : AppOpenSettingsManager {

    override fun getAppOpenSettings(): AppOpenSettings {
        val uuid = appUUID
        val version = appVersion
        val oldVersion = appOldVersion ?: version
        val updateDate = appUpdateDate

        return AppOpenSettings(
            "android",
            uuid,
            version,
            oldVersion,
            updateDate
        )
    }

    override fun setUpdateDate() {
        val version = appVersion
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

    private val appVersion: String
        get() {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                ""
            }
        }

    private val appOldVersion: String?
        get() {
            return preferences.loadString(Constants.spk_nstack_old_version)
        }

    private val appUpdateDate: Date
        get() {
            return preferences.loadString(Constants.spk_nstack_last_updated).iso8601Date
        }
}
