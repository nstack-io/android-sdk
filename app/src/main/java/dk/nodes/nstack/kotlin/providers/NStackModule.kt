package dk.nodes.nstack.kotlin.providers

import android.content.Context
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManagerImpl
import dk.nodes.nstack.kotlin.managers.LiveEditManager
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.util.Preferences
import dk.nodes.nstack.kotlin.util.PreferencesImpl
import kotlin.reflect.KClass

/**
 * Provides dependencies for NStack
 */
class NStackModule(private val context: Context) {

    /**
     * Creates new AppOpenSettingsManager
     */
    fun provideAppOpenSettingsManager(): AppOpenSettingsManagerImpl {
        return getLazyDependency(AppOpenSettingsManagerImpl::class) {
            AppOpenSettingsManagerImpl(
                    context,
                    providePreferences()
            )
        }
    }

    /**
     * Creates new PrefManager
     */
    fun providePrefManager(): PrefManager {
        return getLazyDependency(PrefManager::class) { PrefManager(providePreferences()) }
    }

    /**
     * Creates Network Manager
     */
    fun provideNetworkManager(): NetworkManager {
        return getLazyDependency(NetworkManager::class) { NetworkManager() }
    }

    /**
     * Creates LiveEdit Manager
     */
    fun provideLiveEditManager(): LiveEditManager {
        return getLazyDependency(LiveEditManager::class) { LiveEditManager(provideNetworkManager(), provideAppOpenSettingsManager()) }
    }

    private fun providePreferences(): Preferences {
        return getLazyDependency(PreferencesImpl::class) { PreferencesImpl(context) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getLazyDependency(clazz: KClass<T>, block: () -> T): T {
        if (!dependenciesMap.containsKey(clazz)) {
            dependenciesMap[clazz] = block()
        }
        return dependenciesMap[clazz] as T
    }

    private val dependenciesMap = mutableMapOf<KClass<*>, Any>()
}
