package dk.nodes.nstack.kotlin.provider

class NMetaInterceptor(
    private val environment: String,
    private val versionName: String,
    private val versionRelease: String,
    private val model: String
) : okhttp3.Interceptor {

    @Throws(java.io.IOException::class)
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .header("Accept", "application/vnd.nodes.v1+json")
            .header(
                "N-Meta",
                "android;$environment;$versionName;$versionRelease;$model"
            )
            .build()

        return chain.proceed(newRequest)
    }
}
