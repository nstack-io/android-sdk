package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.util.*
import java.util.*

/**
 * Manages app open settings
 */
class AppOpenSettingsManager(
    private val contextWrapper: ContextWrapper,
    private val preferences: Preferences
) {

    fun getAppOpenSettings(): AppOpenSettings {
        val uuid = appUUID
        val version = contextWrapper.version
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

    fun setUpdateDate() {
        val version = contextWrapper.version
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
            return preferences.loadString(Constants.spk_nstack_old_version).takeUnless { it.isEmpty() }
        }

    private val appUpdateDate: Date
        get() {
            return preferences.loadString(Constants.spk_nstack_last_updated).iso8601Date
        }

}
