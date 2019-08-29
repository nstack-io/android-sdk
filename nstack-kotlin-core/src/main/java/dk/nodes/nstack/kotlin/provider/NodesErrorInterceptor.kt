package dk.nodes.nstack.kotlin.provider

import com.google.gson.Gson
import dk.nodes.nstack.kotlin.models.NStackErrorBody
import dk.nodes.nstack.kotlin.models.NStackException
import okhttp3.Interceptor
import okhttp3.Response

internal class NodesErrorInterceptor(private val gson: Gson) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (!response.isSuccessful) {
            val responseString = response.body()?.string()
            val error = gson.fromJson(
                responseString,
                NStackErrorBody::class.java
            )
            throw NStackException(error)
        } else {
            return response
        }
    }
}