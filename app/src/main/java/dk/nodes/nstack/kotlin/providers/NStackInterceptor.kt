package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.NStack

class NStackInterceptor : okhttp3.Interceptor {
    @Throws(java.io.IOException::class)
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
                //Commented this out because it was causing issues with the cached languageHeader
                //.header("Accept-Language", NStack.getStack().getSelectedLanguageHeader())
                .header("X-Application-Id", NStack.appId)
                .header("X-Rest-Api-Key", NStack.appKey)
                .build()

        return chain.proceed(newRequest)
    }

}
