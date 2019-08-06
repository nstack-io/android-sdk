package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.NStack
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object HttpClientProvider {

    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val WRITE_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 10L

    private fun provideNStackInterceptor(): Interceptor {
        return NStackInterceptor()
    }

    private fun provideHttpLoggingInterceptor(): Interceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (NStack.debugMode) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logging
    }

    private fun provideNMetaInterceptor(): Interceptor {
        return NMetaInterceptor()
    }

    fun getHttpClient(): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(provideNStackInterceptor())
            .addInterceptor(provideNMetaInterceptor())
            .addInterceptor(provideHttpLoggingInterceptor())
            .build()
    }
}
