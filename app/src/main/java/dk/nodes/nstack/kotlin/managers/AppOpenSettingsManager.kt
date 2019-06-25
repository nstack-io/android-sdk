package dk.nodes.nstack.kotlin.managers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.Constants
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.formatted
import dk.nodes.nstack.kotlin.util.iso8601Date
import java.util.*

class AppOpenSettingsManager(private val context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getAppOpenSettings(): AppOpenSettings {
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

    fun setUpdateDate() {
        val version = appVersion
        val updateDate = Date().formatted

        setString(Constants.spk_nstack_last_updated, updateDate)
        setString(Constants.spk_nstack_old_version, version)
    }

    /** App Settings Stuff **/

    private val appUUID: String
        get() {
            NLog.d(this, "Getting UUID")

            var uuid = getString(Constants.spk_nstack_guid)

            if (uuid == null) {
                NLog.d(this, "UUID missing -> Generating!")
                uuid = UUID.randomUUID().toString()
                setString(Constants.spk_nstack_guid, uuid)
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
            return getString(Constants.spk_nstack_old_version)
        }

    private val appUpdateDate: Date
        get() {
            return getString(Constants.spk_nstack_last_updated)?.iso8601Date ?: Date(0)
        }

    /**
     * Helper Functions
     */

    private fun setString(key: String, value: String) {
        prefs.edit()
            .putString(key, value)
            .apply()
    }

    private fun getString(key: String): String? {
        return prefs.getString(key, null)
    }
}
