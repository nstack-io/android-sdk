package dk.nodes.nstack.providers

import android.os.Build
import dk.nodes.nstack.kotlin.NStack

/**
 * Created by joso on 08/10/15.
 */
class NMetaInterceptor(val environment : String = "development") : okhttp3.Interceptor {
    @Throws(java.io.IOException::class)
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
                .header("Accept", "application/vnd.nodes.v1+json")
                .header("N-Meta", "android;${environment};${NStack.clientAppInfo.versionName} (${NStack.clientAppInfo.versionCode});${Build.VERSION.RELEASE};${Build.MODEL}")
                .build()

        return chain.proceed(newRequest)
    }

}
