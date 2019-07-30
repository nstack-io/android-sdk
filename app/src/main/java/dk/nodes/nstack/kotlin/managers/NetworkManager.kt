package dk.nodes.nstack.kotlin.managers

import android.content.Context
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.AppUpdate
import dk.nodes.nstack.kotlin.providers.HttpClientProvider
import dk.nodes.nstack.kotlin.util.parseFromString
import dk.nodes.nstack.kotlin.util.toFormattedString
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class NetworkManager(context: Context) {
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
                .add("last_updated", settings.lastUpdated.toFormattedString())

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


    fun postProposal(
            settings: AppOpenSettings,
            locale: String,
            key: String,
            section: String,
            newValue: String,
            onSuccess: () -> Unit,
            onError: (IOException) -> Unit
    ) {

        val formBuilder = FormBody.Builder()
                .add("key", key)
                .add("section", section)
                .add("value", newValue)
                .add("locale", locale)
                .add("guid", settings.guid)
                .add("platform", "mobile")

        val request = Request.Builder()
                .url("https://nstack.io//api/v2/content/localize/proposals")
                .post(formBuilder.build())
                .build()

        client.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onError.invoke(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val jsonObject = JSONObject(response.body()?.string())
                        if (jsonObject.has("data")) {
                            onSuccess.invoke()
                        } else {
                            onError.invoke(IOException())
                        }
                    }
                })
    }
}
