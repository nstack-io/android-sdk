package dk.nodes.nstack.kotlin.providers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.features.mainmenu.presentation.MainMenuDisplayer
import dk.nodes.nstack.kotlin.features.terms.data.TermsRepository
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.ClassTranslationManager
import dk.nodes.nstack.kotlin.managers.ConnectionManager
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.managers.ViewTranslationManager
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.models.NStackMeta
import dk.nodes.nstack.kotlin.usecases.HandleLocalizeListUseCase
import dk.nodes.nstack.kotlin.util.extensions.ContextWrapper
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

internal class NStackKoinComponent : KoinComponent {
    val nstackMeta: NStackMeta by inject()
    val classTranslationManager: ClassTranslationManager by inject()
    val viewTranslationManager: ViewTranslationManager by inject { parametersOf(NStack.translationHolder) }
    val assetCacheManager: AssetCacheManager by inject()
    val connectionManager: ConnectionManager by inject()
    val appInfo: ClientAppInfo by inject()
    val networkManager: NetworkManager by inject()
    val appOpenSettingsManager: AppOpenSettingsManager by inject()
    val prefManager: PrefManager by inject()
    val contextWrapper: ContextWrapper by inject()
    val mainMenuDisplayer: MainMenuDisplayer by inject()
    val termsRepository: TermsRepository by inject()
    val processScope: LifecycleCoroutineScope by inject()
    val processLifecycle: Lifecycle by inject()
    // Use cases
    val handleLocalizeListUseCase: HandleLocalizeListUseCase by inject()
}