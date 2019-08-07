package dk.nodes.nstack.kotlin.providers

import android.content.Context
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManagerImpl
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.PrefManager

internal class ManagersModule(
    private val nStackModule: NStackModule,
    private val context: Context
) {
    fun provideAppOpenSettingsManager(): AppOpenSettingsManager {
        return AppOpenSettingsManagerImpl(nStackModule.provideClientAppInfo(), nStackModule.providePreferences())
    }

    fun provideAssetCacheManager(): AssetCacheManager {
        return AssetCacheManager(nStackModule.provideContextWrapper())
    }

    fun providePrefManager(): PrefManager {
        return PrefManager(nStackModule.providePreferences())
    }
}