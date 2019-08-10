package dk.nodes.nstack.kotlin.providers

import android.content.Context
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.util.NLog
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object HttpClientProvider {

    private fun providesCache(context: Context): Cache? {
        return try {
            val cacheDirectory = context.cacheDir
            val cacheSize = 10 * 1024 * 1024 // 10 MiB
            Cache(cacheDirectory, cacheSize.toLong())
        } catch (e: Exception) {
            NLog.e(this, "Error", e)
            null
        }
    }

    val metaInterceptor: NMetaInterceptor by lazy { NMetaInterceptor() }

    private fun providesNStackInterceptor(): Interceptor {
        return NStackInterceptor()
    }

    private fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = okhttp3.logging.HttpLoggingInterceptor()
        logging.level = if(NStack.debugMode) okhttp3.logging.HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logging
    }

    fun getHttpClient(context: Context): OkHttpClient {
        val client = OkHttpClient()
                .newBuilder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)

        val cache = providesCache(context)
        val nStackInterceptor = providesNStackInterceptor()
        val loggingInterceptor = providesHttpLoggingInterceptor()

        client.addInterceptor(nStackInterceptor)
        client.addInterceptor(loggingInterceptor)
        client.addInterceptor(metaInterceptor)
        client.cache(cache)

        return client.build()
    }

}
