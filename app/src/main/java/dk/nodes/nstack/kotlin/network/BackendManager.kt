package dk.nodes.nstack.kotlin.network

import dk.nodes.nstack.kotlin.appopen.AppOpenSettings
import dk.nodes.nstack.kotlin.util.nLog
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class BackendManager(var client: OkHttpClient) {
    val TAG = "BackendManager"

    fun getAllLanguagesAsync() : Deferred<Response?> = async(CommonPool) {
        try {
            val request = Request.Builder()
                    .url("https://nstack.io/api/v1/translate/mobile/languages")
                    .build()
            val response = client.newCall(request).execute()
            response
        }
        catch(e : Exception)
        {
            nLog(TAG, "Exception: $e.message")
            null
        }
    }

    fun getAllTranslationsAsync() : Deferred<Response?> = async(CommonPool) {
        try {
            val request = Request.Builder()
                    .url("https://nstack.io/api/v1/translate/mobile/keys?all=true&flat=false")
                    .build()
            val response = client.newCall(request).execute()
            response
        }
        catch(e : Exception)
        {
            nLog(TAG, "Exception: $e.message")
            null
        }
    }

    fun postAppOpen(settings : AppOpenSettings, acceptLanguage : String) : Deferred<Response?> = async(CommonPool) {
        try {
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

            val response = client.newCall(request).execute()

            response
        }
        catch(e : Exception)
        {
            nLog(TAG, "Exception: $e.message")
            null
        }
    }

    fun test()
    {
        async(CommonPool) {
            val lang = getAllLanguagesAsync()
            lang.await()
        }
    }
}
