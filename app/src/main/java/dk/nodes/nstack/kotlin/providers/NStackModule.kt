package dk.nodes.nstack.kotlin.providers

import android.content.Context
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.util.ContextInfo
import dk.nodes.nstack.kotlin.util.Preferences
import dk.nodes.nstack.kotlin.util.PreferencesImpl


/**
 * Provides dependencies for NStack
 */
class NStackModule(private val context: Context) {

    /**
     * Creates new AppOpenSettingsManager
     */
    fun provideAppOpenSettingsManager(): AppOpenSettingsManager {
        return AppOpenSettingsManager(provideContextInfo(), providePreferences())
    }

    /**
     * Creates new PrefManager
     */
    fun providePrefManager(): PrefManager {
        return PrefManager(providePreferences())
    }

    /**
     * Provides context info
     */
    fun provideContextInfo(): ContextInfo {
        return ContextInfo(context)
    }

    private fun providePreferences(): Preferences {
        return PreferencesImpl(context)
    }
}
