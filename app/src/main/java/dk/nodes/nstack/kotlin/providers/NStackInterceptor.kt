package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.NStack
import okhttp3.Interceptor

class NStackInterceptor : Interceptor {
    @Throws(java.io.IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
                //Commented this out because it was causing issues with the cached languageHeader
                //.header("Accept-Language", NStack.getStack().getSelectedLanguageHeader())
                .header("X-Application-Id", NStack.getAppIdKey())
                .header("X-Rest-Api-Key", NStack.getAppApiKey())
                .build()

        return chain.proceed(newRequest)
    }

}
