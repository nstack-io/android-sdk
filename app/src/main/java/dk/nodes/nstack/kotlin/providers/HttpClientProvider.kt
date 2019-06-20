package dk.nodes.nstack.kotlin.providers

import android.content.Context
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

    private fun providesNStackInterceptor(): Interceptor {
        return NStackInterceptor()
    }

    private fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
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
        client.addInterceptor(NMetaInterceptor())
        client.cache(cache)

        return client.build()
    }

}
