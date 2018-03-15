package dk.nodes.nstack.kotlin.managers

import android.content.Context
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.AppUpdate
import dk.nodes.nstack.kotlin.providers.HttpClientProvider
import dk.nodes.nstack.kotlin.util.parseFromString
import okhttp3.*
import java.io.IOException

class NetworkManager(context: Context) {
    private val TAG = NetworkManager::class.java.simpleName
    private val client = HttpClientProvider.getHttpClient(context)
    private val defaultRequestUrl = "https://nstack.io/api/v1/translate/mobile/keys?all=true&flat=false"

    private fun getRequestUrl(): String {
        return NStack.customRequestUrl ?: defaultRequestUrl
    }

    fun loadTranslations(onSuccess: (String) -> Unit, onError: (IOException) -> Unit) {
        val requestUrl = getRequestUrl()

        val request = Request.Builder()
                .url(requestUrl)
                .build()

        client
                .newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onError.invoke(e)
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val responseString = response?.body()?.string()
                        onSuccess.invoke(responseString ?: "")
                    }

                })
    }

    fun postAppOpen(
            settings: AppOpenSettings,
            acceptLanguage: String,
            onSuccess: (AppUpdate) -> Unit,
            onError: (IOException) -> Unit
    ) {
        val formBuilder = FormBody.Builder()
                .add("guid", settings.guid)
                .add("version", settings.version)
                .add("old_version", settings.oldVersion)
                .add("platform", settings.platform)

        val request = Request.Builder()
                .url("https://nstack.io/api/v1/open")
                .header("Accept-Language", acceptLanguage)
                .post(formBuilder.build())
                .build()


        client
                .newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException) {
                        onError.invoke(e)
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val appUpdate = AppUpdate()
                        val responseString = response?.body()?.string()
                        appUpdate.parseFromString(responseString ?: "")
                        onSuccess.invoke(appUpdate)
                    }
                })
    }
}
