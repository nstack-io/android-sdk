package dk.nodes.nstack.kotlin.managers

import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dk.nodes.nstack.kotlin.models.*
import dk.nodes.nstack.kotlin.provider.GsonProvider
import dk.nodes.nstack.kotlin.util.DateDeserializer.Companion.DATE_FORMAT
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class NetworkManager(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val debugMode: Boolean
) {

    private val gson = GsonProvider.provideGson()

    fun loadTranslation(
        url: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        client.newCall(Request.Builder().url(url).build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val json = response.body()!!.string()
                        onSuccess(json.asJsonObject!!.getAsJsonObject("data").toString())
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            })
    }

    suspend fun loadTranslation(url: String): String? {
        val response = client.newCall(Request.Builder().url(url).build()).execute()
        val responseBody = response.body()
        return when {
            response.isSuccessful && responseBody != null -> responseBody.string().asJsonObject
                ?.getAsJsonObject("data").toString()
            else -> null
        }
    }

    fun postAppOpen(
        settings: AppOpenSettings,
        acceptLanguage: String,
        onSuccess: (AppUpdateData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        FormBody.Builder().also {
            it["guid"] = settings.guid
            it["version"] = settings.version
            it["old_version"] = settings.oldVersion
            it["platform"] = settings.platform
            it["last_updated"] = settings.lastUpdated.formatted
            it["dev"] = debugMode.toString()
            it["test"] = settings.versionUpdateTestMode.toString()
        }.buildRequest(
            "$baseUrl/api/v2/open",
            "Accept-Language" to acceptLanguage
        ).call().enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException) {
                onError.invoke(e)
            }

            override fun onResponse(call: Call?, response: Response?) {
                try {
                    val responseString = response?.body()?.string()!!
                    val appUpdate = gson.fromJson(responseString, AppUpdateResponse::class.java)
                    onSuccess.invoke(appUpdate.data)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    suspend fun postAppOpen(settings: AppOpenSettings, acceptLanguage: String): AppOpenResult =
        try {
            FormBody.Builder().also {
                it["guid"] = settings.guid
                it["version"] = settings.version
                it["old_version"] = settings.oldVersion
                it["platform"] = settings.platform
                it["last_updated"] = settings.lastUpdated.formatted
                it["dev"] = debugMode.toString()
                it["test"] = settings.versionUpdateTestMode.toString()
            }.buildRequest(
                "$baseUrl/api/v2/open",
                "Accept-Language" to acceptLanguage
            ).execute().body()?.string()?.let {
                AppOpenResult.Success(gson.fromJson(it, AppUpdateResponse::class.java))
            } ?: AppOpenResult.Failure
        } catch (e: Exception) {
            AppOpenResult.Failure
        }

    /**
     * Notifies the backend that the message has been seen
     */
    fun postMessageSeen(guid: String, messageId: Int) {
        FormBody.Builder().also {
            it["guid"] = guid
            it["message_id"] = messageId.toString()
        }.buildRequest("$baseUrl/api/v1/notify/messages/views")
            .call()
            .enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
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
            .url("$baseUrl/api/v1/notify/rate_reminder/views")
            .post(formBuilder.build())
            .build()

        client
            .newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                }
            })
    }

    /**
     * Get a Collection Response (as String) from NStack collections
     */
    fun getResponse(
        slug: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/api/v1/content/responses/$slug")
            .get()
            .build()

        client
            .newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    onError.invoke(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body()
                    when {
                        response.isSuccessful && responseBody != null -> onSuccess.invoke(
                            responseBody.string()
                        )
                        else -> onError.invoke(RuntimeException("$slug returned: ${response.code()}"))
                    }
                }
            })
    }

    /**
     * Get a Collection Response (as String) synchronously in a coroutine from NStack collections
     */
    suspend fun getResponseSync(slug: String): String? {
        val response = Request.Builder()["$baseUrl/api/v1/content/responses/$slug"]
        val responseBody = response.body()

        return when {
            response.isSuccessful && responseBody != null -> responseBody.string()
            else -> null
        }
    }

    fun postProposal(
        settings: AppOpenSettings,
        locale: String,
        key: String,
        section: String,
        newValue: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        FormBody.Builder().also {
            it["key"] = key
            it["section"] = section
            it["value"] = newValue
            it["locale"] = locale
            it["guid"] = settings.guid
            it["platform"] = "mobile"
        }.buildRequest("$baseUrl/api/v2/content/localize/proposals")
            .call()
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val jsonObject = response.body()?.string()?.asJsonObject
                        if (jsonObject != null && jsonObject.has("data")) {
                            onSuccess()
                        } else {
                            onError(IOException())
                        }
                    }
                }
            )
    }

    fun fetchProposals(
        onSuccess: (List<Proposal>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/api/v2/content/localize/proposals")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseString = response.body()?.string()
                    val listType = object : TypeToken<List<Proposal>>() {}.type
                    val proposals = gson.fromJson<List<Proposal>>(
                            responseString?.asJsonObject?.get("data")!!,
                            listType
                    )
                    onSuccess(proposals)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    fun getLatestTerms(
            termsID: Long,
            acceptLanguage: String,
            settings: AppOpenSettings,
            onSuccess: (TermDetails) -> Unit,
            onError: (Exception) -> Unit
    ) {
        val request = Request.Builder()
                .url("$baseUrl/api/v2/content/terms/$termsID/versions/newest?guid=${settings.guid}")
                .header("Accept-Language", acceptLanguage)
                .get()
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseString = response.body()?.string()
                    val result = gson.fromJson(responseString, TermDetailsResponse::class.java)
                    onSuccess(result.data)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    fun getTerms(
            versionID : Long,
            settings: AppOpenSettings,
            acceptLanguage: String,
            onSuccess: (TermDetails) -> Unit,
            onError: (Exception) -> Unit
    ) {
        val request = Request.Builder()
                .url("$baseUrl/api/v2/content/terms/versions/$versionID?guid=${settings.guid}")
                .header("Accept-Language", acceptLanguage)
                .get()
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseString = response.body()?.string()
                    val result = gson.fromJson(responseString, TermDetailsResponse::class.java)
                    onSuccess(result.data)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    fun acceptTerms(versionID: Long,
                    userID: String,
                    locale: String,
                    settings: AppOpenSettings,
                    onSuccess: () -> Unit,
                    onError: (Exception) -> Unit) {

        val requestBody = FormBody.Builder()
                .add("guid", settings.guid)
                .add("term_version_id", versionID.toString())
                .add("identifier", userID)
                .add("locale", locale)
                .build()

        val request = Request.Builder()
                .url("$baseUrl/api/v2/content/terms/versions/views")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onError(IllegalStateException(response.message()))
                    }
                }
            })
    }

    val String.asJsonObject: JsonObject?
        get() = try {
            gson.fromJson(this, JsonObject::class.java)
        } catch (e: Exception) {
            null
        }

    private val Date.formatted: String
        get() {
            return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(this)
        }

    suspend fun getRateReminder2(
        settings: AppOpenSettings
    ): RateReminder2? {
        return Request.Builder()["$baseUrl/api/v2/notify/rate_reminder_v2?guid=${settings.guid}"]
            .parseJson { RateReminder2(it) }
    }

    suspend fun postRateReminderAction(
        settings: AppOpenSettings,
        action: String
    ) {
        FormBody.Builder().also {
            it["guid"] = settings.guid
            it["action"] = action
        }.post("$baseUrl/api/v2/notify/rate_reminder_v2/events")
    }

    suspend fun postRateReminderAction(
        settings: AppOpenSettings,
        rateReminderId: Int,
        answer: String
    ) {
        FormBody.Builder().also {
            it["guid"] = settings.guid
            it["answer"] = answer
        }.post("$baseUrl/api/v2/notify/rate_reminder_v2/${rateReminderId}/answers")
    }

    suspend fun postFeedback(feedback: Feedback) {
        FormBody.Builder().also {
            it["app_version"] = feedback.appVersion
            it["device"] = feedback.deviceName
            it["name"] = feedback.name
            it["email"] = feedback.email
            it["message"] = feedback.message
        }.post("$baseUrl/api/v2/ugc/feedbacks")
    }

    private operator fun FormBody.Builder.set(field: String, value: String) {
        if (value.isNotEmpty()) {
            add(field, value)
        }
    }

    private operator fun Request.Builder.get(url: String): Response {
        return url(url).get().build().execute()
    }

    private fun Request.Builder.applyHeaders(vararg header: Pair<String, String>): Request.Builder {
        header.forEach { addHeader(it.first, it.second) }
        return this
    }

    private fun FormBody.Builder.buildRequest(url: String): Request {
        return Request.Builder().url(url).post(build()).build()
    }

    private fun FormBody.Builder.buildRequest(
        url: String,
        vararg header: Pair<String, String>
    ): Request {
        return Request.Builder().url(url).applyHeaders(*header).url(url).post(build()).build()
    }

    private fun Request.execute(): Response {
        return client.newCall(this).execute()
    }

    private fun Request.call(): Call {
        return client.newCall(this)
    }

    private fun FormBody.Builder.post(url: String) {
        buildRequest(url).execute()
    }

    private fun <T> Response.parseJson(transform: (JsonObject) -> T): T? {
        if (isSuccessful) {
            return body()?.string()?.asJsonObject?.let { transform(it) }
        }
        return null
    }
}
