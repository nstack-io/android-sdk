package dk.nodes.nstack.kotlin.providers

import android.content.Context
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManagerImpl
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.NetworkManagerImpl
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.managers.ViewTranslationManager
import dk.nodes.nstack.kotlin.provider.TranslationHolder
import dk.nodes.nstack.kotlin.util.Preferences
import dk.nodes.nstack.kotlin.util.PreferencesImpl
import kotlin.reflect.KClass

/**
 * Provides dependencies for NStack
 */
class NStackModule(private val context: Context, private val translationHolder: TranslationHolder) {

    /**
     * Creates new AppOpenSettingsManager
     */
    fun provideAppOpenSettingsManager(): AppOpenSettingsManager {
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
        return getLazyDependency(NetworkManagerImpl::class) { NetworkManagerImpl(HttpClientProvider.getHttpClient()) }
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

    fun provideViewTranslationManager(): ViewTranslationManager {
        return getLazyDependency(ViewTranslationManager::class) {
            ViewTranslationManager(translationHolder)
        }
    }

    companion object {
        private val dependenciesMap = mutableMapOf<KClass<*>, Any>()
    }
}
