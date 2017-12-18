package dk.nodes.nstack.kotlin.appopen

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dk.nodes.nstack.kotlin.nLog
import java.util.*

class AppOpenSettings(context : Context) {
    val TAG : String = "AppOpenSettings"
    val prefs : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val platform = "android"
    var guid: String
    var version: String
    var oldVersion: String
    var lastUpdated: Date

    enum class KEY {
        GUID,
        VERSION,
        OLDVERSION,
        LASTUPDATED
    }

    init {
        try {
            guid = prefs.getString(KEY.GUID.name, UUID.randomUUID().toString())

            version = /* if(prefs.contains(KEY.VERSION.name))
                prefs.getString(KEY.VERSION.name, "")
            else */
                context.packageManager.getPackageInfo(context.packageName, 0).versionName

            oldVersion = if(prefs.contains(KEY.OLDVERSION.name))
                prefs.getString(KEY.OLDVERSION.name, "")
            else
                version

            lastUpdated = Date(prefs.getLong(KEY.LASTUPDATED.name, 0))
        } catch (e: Exception) {
            nLog(TAG, e.message ?: "Unknown exception obtaining versionName from PackageManager")
            throw IllegalStateException("Could not obtain versionName from PackageManager")
        }
    }

}