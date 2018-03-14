package dk.nodes.nstack.kotlin.managers

import android.content.Context
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.AppUpdate
import dk.nodes.nstack.kotlin.providers.HttpClientProvider
import dk.nodes.nstack.kotlin.util.parseFromString
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.FormBody
import okhttp3.Request

class NetworkManager(context: Context) {
    private val TAG = NetworkManager::class.java.simpleName
    private val client = HttpClientProvider.getHttpClient(context)
    private val defaultRequestUrl = "https://nstack.io/api/v1/translate/mobile/keys?all=true&flat=false"

    private fun getRequestUrl(): String {
        return NStack.customRequestUrl ?: defaultRequestUrl
    }

    fun loadTranslations(): Single<String> {
        return Single.create<String> {
            val requestUrl = getRequestUrl()

            val request = Request.Builder()
                    .url(requestUrl)
                    .build()

            val response = client.newCall(request).execute()

            val body = response.body()?.string()

            if (body != null) {
                it.onSuccess(body)
            } else {
                it.onError(Exception("Unable to parse data: $body"))
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun postAppOpen(settings: AppOpenSettings, acceptLanguage: String): Single<AppUpdate> {
        return Single.create<AppUpdate> {
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
            val body = response.body()?.string()
            val appUpdate = AppUpdate()

            appUpdate.parseFromString(body ?: "")

            if (body != null) {
                it.onSuccess(appUpdate)
            } else {
                it.onError(Exception("Unable to parse data: $body"))
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
