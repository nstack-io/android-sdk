package dk.nodes.nstack.kotlin.providers

object HttpClientProvider {
    fun provideHttpClient(cache: okhttp3.Cache?, debug: Boolean): okhttp3.OkHttpClient {
        val client = okhttp3.OkHttpClient()
                .newBuilder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)

        client.addInterceptor(NStackInterceptor())

        if (debug) {
            val logging = okhttp3.logging.HttpLoggingInterceptor()
            logging.level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logging)
        }

        if (cache != null) {
            client.cache(cache)
        }

        return client.build()
    }

}
