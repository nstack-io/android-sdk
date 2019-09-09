package dk.nodes.nstack.kotlin.provider

import okhttp3.Interceptor

class NStackInterceptor(private val appIdKey: String, private val appApiKey: String) : Interceptor {

    @Throws(java.io.IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
                // Commented this out because it was causing issues with the cached languageHeader
                // .header("Accept-Language", NStack.getStack().getSelectedLanguageHeader())
                .header("X-Application-Id", appIdKey)
                .header("X-Rest-Api-Key", appApiKey)
                .build()

        return chain.proceed(newRequest)
    }
}
