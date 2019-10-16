package dk.nodes.nstack.kotlin.provider

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.reflect.KClass

object HttpClientProvider {

    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val WRITE_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 10L

    private fun provideNStackInterceptor(
        appIdKey: String,
        appApiKey: String,
        sdkVersion: String
    ): Interceptor {
        return NStackInterceptor(appIdKey, appApiKey, sdkVersion)
    }

    private fun provideHttpLoggingInterceptor(debugMode: Boolean): Interceptor {
        val logging = HttpLoggingInterceptor()
        logging.level =
            if (debugMode) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logging
    }

    private fun provideNMetaInterceptor(
        environment: String,
        versionName: String,
        versionRelease: String,
        model: String
    ): Interceptor {
        return NMetaInterceptor(
            environment = environment,
            versionName = versionName,
            versionRelease = versionRelease,
            model = model
        )
    }

    fun getHttpClient(
        appIdKey: String,
        appApiKey: String,
        sdkVersion: String,
        environment: String,
        versionName: String,
        versionRelease: String,
        model: String,
        debugMode: Boolean = false
    ): OkHttpClient {
        return getLazyDependency(OkHttpClient::class) {
            OkHttpClient()
                .newBuilder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(provideNStackInterceptor(appIdKey = appIdKey, appApiKey = appApiKey, sdkVersion = sdkVersion))
                .addInterceptor(
                    provideNMetaInterceptor(
                        environment = environment,
                        versionName = versionName,
                        versionRelease = versionRelease,
                        model = model
                    )
                )
                .addInterceptor(provideHttpLoggingInterceptor(debugMode))
                .addInterceptor(NodesErrorInterceptor(GsonProvider.provideGson()))
                .build()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <T : Any> getLazyDependency(
        clazz: KClass<T>,
        crossinline block: () -> T
    ): T {
        if (!dependenciesMap.containsKey(clazz)) {
            dependenciesMap[clazz] = block()
        }
        return dependenciesMap[clazz] as T
    }

    private val dependenciesMap = mutableMapOf<KClass<*>, Any>()
}
