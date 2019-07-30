package dk.nodes.nstack.kotlin.managers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.Constants
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.extensions.parseFromISO8601
import dk.nodes.nstack.kotlin.util.extensions.toFormattedString
import java.util.*

class AppOpenSettingsManager(private val context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getAppOpenSettings(): AppOpenSettings {
        val uuid = getAppUUID()
        val version = getAppVersion()
        val oldVersion = getAppOldVersion() ?: version
        val updateDate = getAppUpdateDate()

        return AppOpenSettings(
                "android",
                uuid,
                version,
                oldVersion,
                updateDate
        )
    }

    fun setUpdateDate() {
        val version = getAppVersion()
        val updateDate = Date().toFormattedString()

        setString(Constants.spk_nstack_last_updated, updateDate)
        setString(Constants.spk_nstack_old_version, version)
    }

    /** App Settings Stuff **/

    private fun getAppUUID(): String {
        NLog.d(this, "Getting UUID")

        var uuid = getString(Constants.spk_nstack_guid)

        if (uuid == null) {
            NLog.d(this, "UUID missing -> Generating!")
            uuid = UUID.randomUUID().toString()
            setString(Constants.spk_nstack_guid, uuid)
        }

        return uuid
    }

    private fun getAppVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            ""
        }
    }

    private fun getAppOldVersion(): String? {
        return getString(Constants.spk_nstack_old_version)
    }

    private fun getAppUpdateDate(): Date {
        val dateString = getString(Constants.spk_nstack_last_updated)

        return if (dateString == null) {
            Date(0)
        } else {
            val date = Date()
            date.parseFromISO8601(dateString)
            date
        }
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