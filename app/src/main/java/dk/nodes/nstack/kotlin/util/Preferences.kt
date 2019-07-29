package dk.nodes.nstack.kotlin.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


/**
 * An interface for shared preferences
 */
internal interface Preferences {

    /**
     * Saves value with key
     */
    fun saveString(key: String, value: String)

    /**
     * Gets string by key
     */
    fun loadString(key: String): String

    /**
     * Loads all strings with key which satisfies predicate
     * @return map of keys to resulting strings
     */
    fun loadStringsWhereKey(predicate: (String) -> Boolean): Map<String, String>
}


/**
 * Preferences implementation
 */
internal class PreferencesImpl(context: Context) : Preferences {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun saveString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun loadString(key: String): String {
        return preferences.getString(key, "") ?: ""
    }

    override fun loadStringsWhereKey(predicate: (String) -> Boolean): Map<String, String> {
        return preferences.all.filterKeys(predicate).mapValues { (it.value as? String) ?: "" }
    }
}
