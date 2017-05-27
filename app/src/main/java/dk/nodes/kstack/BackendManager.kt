package dk.nodes.kstack

import dk.nodes.kstack.appopen.AppOpenSettings
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class BackendManager(var client: OkHttpClient) {
    val TAG = "BackendManager"

    fun getAllLanguagesAsync() = async(CommonPool) {
        val request = Request.Builder()
                .url("https://nstack.io/api/v1/translate/mobile/languages")
                .build()
        val response = client.newCall(request).execute()
        response
    }

    fun getAllTranslationsAsync() = async(CommonPool) {
        val request = Request.Builder()
                .url("https://nstack.io/api/v1/translate/mobile/keys?all=true&flat=false")
                .build()
        val response = client.newCall(request).execute()
        response
    }

    fun postAppOpen(settings : AppOpenSettings, acceptLanguage : String) = async(CommonPool) {
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

    fun test()
    {
        async(CommonPool) {
            val lang = getAllLanguagesAsync()
            lang.await()
        }
    }
}
