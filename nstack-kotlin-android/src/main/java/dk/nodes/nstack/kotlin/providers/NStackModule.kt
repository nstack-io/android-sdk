package dk.nodes.nstack.kotlin.providers

import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import dk.nodes.nstack.BuildConfig
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.managers.ClassTranslationManager
import dk.nodes.nstack.kotlin.managers.ConnectionManager
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.ViewTranslationManager
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.models.NStackMeta
import dk.nodes.nstack.kotlin.provider.HttpClientProvider
import dk.nodes.nstack.kotlin.provider.TranslationHolder
import dk.nodes.nstack.kotlin.util.Preferences
import dk.nodes.nstack.kotlin.util.PreferencesImpl
import dk.nodes.nstack.kotlin.util.extensions.ContextWrapper
import org.koin.dsl.module

val nStackModule = module {

    single<Preferences> {
        PreferencesImpl(get())
    }

    single { (translationHolder: TranslationHolder) ->
        ViewTranslationManager(translationHolder)
    }

    single {
        NetworkManager(
            get(),
            NStack.baseUrl,
            NStack.debugMode,
            get()
        )
    }

    single {
        ClientAppInfo(get())
    }

    single {
        NStackMeta(get())
    }

    single {
        ConnectionManager(get())
    }

    single {
        ClassTranslationManager()
    }

    single {
        ContextWrapper(get())
    }

    single {
        ProcessLifecycleOwner.get().lifecycle.coroutineScope
    }

    single {
        HttpClientProvider.getHttpClient(
            appIdKey = NStack.appIdKey,
            appApiKey = NStack.appApiKey,
            sdkVersion = BuildConfig.SDK_VERSION,
            environment = NStack.env,
            versionName = NStack.appClientInfo.versionName,
            versionRelease = Build.VERSION.RELEASE,
            model = Build.MODEL,
            debugMode = NStack.debugMode
        )
    }
}
