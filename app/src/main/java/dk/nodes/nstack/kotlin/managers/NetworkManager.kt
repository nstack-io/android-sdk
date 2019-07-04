package dk.nodes.nstack.kotlin.managers

import android.content.Context
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.AppUpdateData
import dk.nodes.nstack.kotlin.models.AppUpdateResponse
import dk.nodes.nstack.kotlin.providers.HttpClientProvider
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.asJsonObject
import dk.nodes.nstack.kotlin.util.formatted
import okhttp3.*
import java.io.IOException

class NetworkManager(context: Context) {

    private val client = HttpClientProvider.getHttpClient(context)

    fun loadTranslation(url: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        client.newCall(Request.Builder().url(url).build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val translations = response.body()!!.string().asJsonObject!!.getJSONObject("data")
                        onSuccess(translations.toString())
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            })
    }

    fun postAppOpen(
        settings: AppOpenSettings,
        acceptLanguage: String,
        onSuccess: (AppUpdateData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val formBuilder = FormBody.Builder()
            .add("guid", settings.guid)
            .add("version", settings.version)
            .add("old_version", settings.oldVersion)
            .add("platform", settings.platform)
            .add("last_updated", settings.lastUpdated.formatted)
            .add("dev", NStack.debugMode.toString())

        val request = Request.Builder()
            .url("${NStack.baseUrl}/api/v2/open")
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
                    try {
                        val responseString = response?.body()?.string()!!
                        val appUpdate = AppUpdateResponse(responseString.asJsonObject!!)
                        onSuccess.invoke(appUpdate.data)
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            })
    }

    /**
     * Notifies the backend that the message has been seen
     */
    fun postMessageSeen(guid: String, messageId: Int) {
        val formBuilder = FormBody.Builder()
            .add("guid", guid)
            .add("message_id", messageId.toString())

        val request = Request.Builder()
            .url("${NStack.baseUrl}/api/v1/notify/messages/views")
            .post(formBuilder.build())
            .build()

        client
            .newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    NLog.e(this, "Failure posting message seen", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    NLog.v(this, "Message seen")
                }
            })
    }

    /**
     * Notifies the backend that the rate reminder has been seen
     */
    fun postRateReminderSeen(appOpenSettings: AppOpenSettings, rated: Boolean) {
        val answer = if (rated) "yes" else "no"

        val formBuilder = FormBody.Builder()
            .add("guid", appOpenSettings.guid)
            .add("platform", appOpenSettings.platform)
            .add("answer", answer)

        val request = Request.Builder()
            .url("${NStack.baseUrl}/api/v1/notify/rate_reminder/views")
            .post(formBuilder.build())
            .build()

        client
            .newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    NLog.e(this, "Failure posting rate reminder seen", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    NLog.v(this, "Rate reminder seen")
                }
            })
    }
}
