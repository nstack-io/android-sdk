package dk.nodes.nstack.kotlin.providers

import android.os.Build
import dk.nodes.nstack.kotlin.NStack

class NMetaInterceptor() : okhttp3.Interceptor {

    private var environment = "production"
    private var osVersion = "unknown"
    private var phoneModel = "unknown"
    var initialized = false

    fun setupMetaHeaders(environment: String, osVersion: String, phoneModel: String) {
        this.environment = environment
        this.osVersion = osVersion
        this.phoneModel = phoneModel
        initialized = true
    }

    @Throws(java.io.IOException::class)
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
                .header("Accept", "application/vnd.nodes.v1+json")
                .header(
                        "N-Meta",
                        "android;$environment;${NStack.getAppClientInfo().versionName};$osVersion;$phoneModel"
                )
                .build()

        return chain.proceed(newRequest)
    }

}
