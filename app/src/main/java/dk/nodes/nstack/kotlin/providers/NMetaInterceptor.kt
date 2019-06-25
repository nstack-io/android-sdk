package dk.nodes.nstack.kotlin.providers

import android.os.Build
import dk.nodes.nstack.kotlin.NStack

class NMetaInterceptor(private val environment: String = NStack.env) : okhttp3.Interceptor {

    @Throws(java.io.IOException::class)
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
                .header("Accept", "application/vnd.nodes.v1+json")
                .header(
                        "N-Meta",
                        "android;$environment;${NStack.getAppClientInfo().versionName} (${NStack.getAppClientInfo().versionCode});${Build.VERSION.RELEASE};${Build.MODEL}"
                )
                .build()

        return chain.proceed(newRequest)
    }

}
