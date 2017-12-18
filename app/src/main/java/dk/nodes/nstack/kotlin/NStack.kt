package dk.nodes.nstack.kotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AlertDialog
import android.util.Log
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.appopen.AppOpenSettings
import dk.nodes.nstack.kotlin.appopen.AppUpdate
import dk.nodes.nstack.kotlin.models.Language
import dk.nodes.nstack.kotlin.util.LocaleUtils
import dk.nodes.nstack.kotlin.models.StoreId
import dk.nodes.nstack.kotlin.models.UpdateType
import dk.nodes.nstack.kotlin.providers.HttpCacheProvider
import dk.nodes.nstack.kotlin.providers.HttpClientProvider
import dk.nodes.nstack.kotlin.store.JsonStore
import dk.nodes.nstack.kotlin.store.PrefJsonStore
import dk.nodes.nstack.kotlin.translate.TranslationManager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("StaticFieldLeak")
object NStack {
    val TAG = "NStack"
    private lateinit var appContext: Context
    var appId: String = ""
    var appKey: String = ""
    var isInitialized: Boolean = false
    private lateinit var backendManager: BackendManager
    private val translationManager = TranslationManager()
    private var updateJob: Job? = null
    private var appOpenJob: Job? = null
    private lateinit var store: JsonStore
    private lateinit var appOpenSettings: AppOpenSettings
    private var jsonLanguages: JSONObject? = null
    private var jsonTranslations: JSONObject? = null
    private val localeLanguageMap: MutableMap<String, Language> = HashMap()
    private var appUpdate: AppUpdate? = null
    lateinit var clientAppInfo: ClientAppInfo

    // Properties with custom setters/getters ---------------------------------------------------
    var currentLocale: String? = null
        set(value) {
            field = value
            //nLog(TAG, "Current locale set to $value")
        }

    fun getCurrentLocale(): Locale {
        return LocaleUtils.getLocalFromString(localeString = currentLocale)
    }

    fun getAvailableLanguages(): MutableMap<String, Language> {
        return localeLanguageMap
    }

    var debug: Boolean = false
        set(value) {
            if (!isInitialized)
                throw IllegalStateException("init() was not called")
            field = value
            backendManager.client = HttpClientProvider.provideHttpClient(HttpCacheProvider.provideCache(appContext), value)
        }

    val deviceLocale: String
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return Locale.getDefault().toString().replace("_", "-")
            }
            return Locale.getDefault().toLanguageTag()
        }

    // Public interface --------------------------------------------------------------
    fun init(appContext: Context, appId: String, appKey: String, debug: Boolean = false) {
        if (isInitialized)
            return
        NStack.appContext = appContext
        clientAppInfo = ClientAppInfo(appContext)
        nLog(TAG, "Client App Info: package = ${clientAppInfo.packageName}, versionName = ${clientAppInfo.versionName}, versionCode = ${clientAppInfo.versionCode}")
        NStack.appId = appId
        NStack.appKey = appKey
        nLog(TAG, "AppId = $appId, AppKey = $appKey")
        backendManager = BackendManager(HttpClientProvider.provideHttpClient(HttpCacheProvider.provideCache(appContext), debug))
        store = PrefJsonStore(appContext)
        appOpenSettings = AppOpenSettings(appContext)
        isInitialized = true
        NStack.debug = debug

        updateCache()
        async(CommonPool)
        {
            updateJob?.join()
            // build map of available nstack languages indexed by language tag
            if (jsonLanguages != null)
                buildLocaleLanguageMap(jsonLanguages!!)
            // if device language matches a downloaded or cached language, update translation class
            if (jsonTranslations != null)
                updateTranslationClass(jsonTranslations!!)
        }

        nLog(TAG, "Just ran updateCacheAsync")

    }

    fun appOpen(callback: AppOpenCallback = {}) {
        if (!isInitialized)
            throw IllegalStateException("init() was not called")
        appOpenJob = launch(CommonPool)
        {
            // if Update job is still running, wait for it
            updateJob?.join()
            val response = backendManager.postAppOpen(appOpenSettings, currentLocale ?: deviceLocale).await()
            if (response != null) {
                if (response.isSuccessful)
                    parseAppOpenResponse(response)
                launch(UI)
                {
                    callback(response.isSuccessful)
                }
            } else {
                launch(UI)
                {
                    callback(false)
                }
            }
        }
    }

    fun versionControl(activity: Activity, callback: VersionControlCallback) {
        if (!isInitialized)
            throw IllegalStateException("init() was not called")
        // we launch this async in case we need to wait for the updateJob to complete
        launch(CommonPool)
        {
            // if Update job is still running, wait for it
            updateJob?.join()
            // if app open is still running, wait for it
            appOpenJob?.join()

            launch(UI)
            {
                if (appUpdate?.isUpdate ?: false) {
                    val builder = AlertDialog.Builder(activity, R.style.znstack_DialogStyle)
                    builder.setTitle(appUpdate?.title)
                    builder.setMessage(appUpdate?.message)
                    builder.setPositiveButton(appUpdate?.positiveBtn ?: "Ok", { dialog, which ->
                        try {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(appUpdate?.link))
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            appContext.startActivity(i)
                        } catch (e: Exception) {
                            nLog(TAG, e.message ?: "Exception opening google play")
                        }
                    })
                            .setCancelable(!(appUpdate?.force ?: false))

                    if (appUpdate?.force ?: false)
                        callback(UpdateType.FORCE_UPDATE, builder)
                    else
                        callback(UpdateType.UPDATE, builder)
                } else    // we never got the app open object for some reason or isUpdate was false
                    callback(UpdateType.NOTHING, null)

            }
        }
    }

    fun setLogFunction(fnc: LogFunction) {
        nLog = fnc
    }

    fun setTranslationClass(translationClass: Class<*>) {
        translationManager.setTranslationClass(translationClass)
    }

    fun translate(view: Any) {
        translationManager.translate(view)
    }

    fun setLanguage(locale: Locale) {
        Log.e(TAG, "Setting Language: " + locale.toString())

        this.currentLocale = locale.toString()

        updateTranslationClass(jsonTranslations!!)
    }

    fun setLanguage(locale: String) {
        var newLocale = locale

        if (newLocale.contains("_")) {
            newLocale = newLocale.replace("_", "-")
        }

        Log.e(TAG, "Setting Language: " + newLocale)

        this.currentLocale = newLocale

        updateTranslationClass(jsonTranslations!!)
    }

    // private fun -------------------------------------------------------------------------------
    private fun buildLocaleLanguageMap(languages: JSONObject) {
        val lang_array = languages.getJSONArray("data")
        repeat(lang_array.length()) { i ->
            val json_lang: JSONObject = lang_array.getJSONObject(i)
            val lang: Language = Language(json_lang.getInt("id"), json_lang.getString("name"), json_lang.getString("locale"), json_lang.getString("direction"))
            localeLanguageMap[lang.locale] = lang
        }
        nLog(TAG, "Language map = ${localeLanguageMap}")
    }

    private fun updateTranslationClass(translations: JSONObject) {
        val locale = currentLocale ?: deviceLocale
        nLog(TAG, "Attempting to update translation class with locale $locale")
        val data = translations.getJSONObject("data")
        val iterator = data.keys()
        while (iterator.hasNext()) {
            val langTag = iterator.next()
            nLog(TAG, langTag)

            if (locale.toLowerCase().contentEquals(langTag.toLowerCase())) {
                nLog(TAG, "Found matching locale in stored translations, overriding baked in language with $langTag")
                translationManager.parseTranslations(data.getJSONObject(langTag))
            }
        }
    }

    private fun parseAndSave(key: String, response: Response?): JSONObject? {
        response?.let {
            if (response.isSuccessful) {
                try {
                    val obj: JSONObject = JSONObject(response.body()?.string())
                    store.save(key, obj, {
                        nLog(TAG, "Saved $key to JsonStore")
                    })
                    return obj
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun updateCache() {
        updateJob = launch(CommonPool) {
            jsonLanguages = store.loadDeferred(StoreId.LANGUAGES.name).await()
            jsonTranslations = store.loadDeferred(StoreId.TRANSLATIONS.name).await()

            if (jsonLanguages == null) {
                jsonLanguages = parseAndSave(StoreId.LANGUAGES.name, backendManager.getAllLanguagesAsync().await())
            }
            if (jsonTranslations == null) {
                jsonTranslations = parseAndSave(StoreId.TRANSLATIONS.name, backendManager.getAllTranslationsAsync().await())
            }
        }
    }

    private fun parseAppOpenResponse(response: Response) {
        try {
            val obj: JSONObject = JSONObject(response.body()?.string())
            val data: JSONObject = obj.getJSONObject("data")
            // set current locale
            currentLocale = obj.getJSONObject("meta").getJSONObject("language").getString("locale")
            val translate: JSONObject? = data.getJSONObject("translate")
            if (translate != null)
                translationManager.parseTranslations(translate)
            if (data.has("update"))
                appUpdate = AppUpdate(data.getJSONObject("update"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

}