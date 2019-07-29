package dk.nodes.nstack.kotlin.providers

import android.content.Context
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.util.ContextWrapper
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
     * Creates new AssetCacheManager
     */
    fun provideAssetCacheManager(): AssetCacheManager {
        return AssetCacheManager(provideContextInfo())
    }

    /**
     * Creates new PrefManager
     */
    fun providePrefManager(): PrefManager {
        return PrefManager(providePreferences())
    }

    private fun provideContextInfo(): ContextWrapper {
        return ContextWrapper(context)
    }

    private fun providePreferences(): Preferences {
        return PreferencesImpl(context)
    }
}
