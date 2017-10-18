package dk.nodes.kstack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AlertDialog
import dk.nodes.kstack.appopen.AppOpenSettings
import dk.nodes.kstack.appopen.AppUpdate
import dk.nodes.kstack.providers.HttpCacheProvider
import dk.nodes.kstack.providers.HttpClientProvider
import dk.nodes.kstack.translate.TranslationManager
import dk.nodes.kstack.store.JsonStore
import dk.nodes.kstack.store.PrefJsonStore
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by bison on 22-05-2017.
 */

// log function defintion, yay typedef is back :D
typealias LogFunction = (tag : String, msg : String) -> Unit

enum class UpdateType {
    UPDATE,
    FORCE_UPDATE,
    //CHANGELOG, <-- fuck you changelog, nobody use you!!
    NOTHING
}

enum class StoreId
{
    LANGUAGES,
    TRANSLATIONS
}

// data/model class for storing information about available languages
data class Language (var id : Int, var name : String, var locale : String, var direction : String)

typealias VersionControlCallback = (type : UpdateType, builder : AlertDialog.Builder?) -> Unit
typealias AppOpenCallback = (success: Boolean) -> Unit

/*
    Default implementation of internal logger
*/
internal var kLog : LogFunction = fun (tag, msg) {
    println("$tag : $msg")
}

object KStack {
    val TAG = "KStack"
    private lateinit var appContext : Context
    var appId : String = ""
    var appKey : String = ""
    var isInitialized : Boolean = false
    private lateinit var backendManager : BackendManager
    private val translationManager = TranslationManager()
    private var updateJob : Job? = null
    private var appOpenJob : Job? = null
    private lateinit var store: JsonStore
    private lateinit var appOpenSettings : AppOpenSettings
    private var jsonLanguages : JSONObject? = null
    private var jsonTranslations : JSONObject? = null
    private val localeLanguageMap : MutableMap<String, Language> = HashMap()
    private var appUpdate : AppUpdate? = null
    lateinit var clientAppInfo : ClientAppInfo

    // Properties with custom setters/getters ---------------------------------------------------
    var currentLocale : String? = null
        set(value) {
            field = value
            //kLog(TAG, "Current locale set to $value")
        }

    var debug : Boolean = false
        set(value) {
            if(!isInitialized)
                throw IllegalStateException("init() was not called")
            field = value
            backendManager.client = HttpClientProvider.provideHttpClient(HttpCacheProvider.provideCache(appContext), value)
        }

    val deviceLocale: String
        get() {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return Locale.getDefault().toString().replace("_", "-")
            }
            return Locale.getDefault().toLanguageTag()
        }

    // Public interface --------------------------------------------------------------
    fun init(appContext : Context, appId : String, appKey : String, debug : Boolean = false)
    {
        if(isInitialized)
            return
        KStack.appContext = appContext
        clientAppInfo = ClientAppInfo(appContext)
        kLog(TAG, "Client App Info: package = ${clientAppInfo.packageName}, versionName = ${clientAppInfo.versionName}, versionCode = ${clientAppInfo.versionCode}")
        KStack.appId = appId
        KStack.appKey = appKey
        kLog(TAG, "AppId = $appId, AppKey = $appKey")
        backendManager = BackendManager(HttpClientProvider.provideHttpClient(HttpCacheProvider.provideCache(appContext), debug))
        store = PrefJsonStore(appContext)
        appOpenSettings = AppOpenSettings(appContext)
        isInitialized = true
        KStack.debug = debug

        updateCache()
        async(CommonPool)
        {
            updateJob?.join()
            // build map of available nstack languages indexed by language tag
            if(jsonLanguages != null)
                buildLocaleLanguageMap(jsonLanguages!!)
            // if device language matches a downloaded or cached language, update translation class
            if(jsonTranslations != null)
                updateTranslationClass(jsonTranslations!!)
        }

        kLog(TAG, "Just ran updateCacheAsync")

    }

    fun appOpen(callback: AppOpenCallback = {})
    {
        if(!isInitialized)
            throw IllegalStateException("init() was not called")
        appOpenJob = launch(CommonPool)
        {
            // if Update job is still running, wait for it
            updateJob?.join()
            val response = backendManager.postAppOpen(appOpenSettings, currentLocale ?: deviceLocale).await()
            if(response != null) {
                if (response.isSuccessful)
                    parseAppOpenResponse(response)
                launch(UI)
                {
                    callback(response.isSuccessful)
                }
            }
            else
            {
                launch(UI)
                {
                    callback(false)
                }
            }
        }
    }

    fun versionControl(activity : Activity, callback : VersionControlCallback)
    {
        if(!isInitialized)
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
                if(appUpdate?.isUpdate ?: false)
                {
                    val builder = AlertDialog.Builder(activity, R.style.znstack_DialogStyle)
                    builder.setTitle(appUpdate?.title)
                    builder.setMessage(appUpdate?.message)
                    builder.setPositiveButton(appUpdate?.positiveBtn ?: "Ok", {dialog, which ->
                        try {
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(appUpdate?.link))
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            appContext.startActivity(i)
                        } catch (e: Exception) {
                            kLog(TAG, e.message ?: "Exception opening google play")
                        }
                    })
                    .setCancelable(!(appUpdate?.force ?: false))

                    if(appUpdate?.force ?: false)
                        callback(UpdateType.FORCE_UPDATE, builder)
                    else
                        callback(UpdateType.UPDATE, builder)
                }
                else    // we never got the app open object for some reason or isUpdate was false
                    callback(UpdateType.NOTHING, null)

            }
        }
    }

    fun setLogFunction(fnc : LogFunction)
    {
        kLog = fnc
    }

    fun setTranslationClass(translationClass : Class<*>)
    {
        translationManager.setTranslationClass(translationClass)
    }

    fun translate(view: Any)
    {
        translationManager.translate(view)
    }

    // private fun -------------------------------------------------------------------------------
    private fun buildLocaleLanguageMap(languages: JSONObject) {
        val lang_array = languages.getJSONArray("data")
        repeat(lang_array.length()) { i ->
            val json_lang : JSONObject = lang_array.getJSONObject(i)
            val lang : Language = Language(json_lang.getInt("id"), json_lang.getString("name"), json_lang.getString("locale"), json_lang.getString("direction"))
            localeLanguageMap[lang.locale] = lang
        }
        kLog(TAG, "Language map = $localeLanguageMap")
    }

    private fun updateTranslationClass(translations : JSONObject)
    {
        val locale = currentLocale ?: deviceLocale
        kLog(TAG, "Attempting to update translation class with locale $locale")
        val data = translations.getJSONObject("data")
        val iterator = data.keys()
        while (iterator.hasNext()) {
            val langTag = iterator.next()
            kLog(TAG, langTag)

            if(locale.toLowerCase().contentEquals(langTag.toLowerCase()))
            {
                kLog(TAG, "Found matching locale in stored translations, overriding baked in language with $langTag")
                translationManager.parseTranslations(data.getJSONObject(langTag))
            }
        }
    }

    private fun parseAndSave(key : String, response : Response?) : JSONObject?
    {
        response?.let {
            if(response.isSuccessful)
            {
                try {
                    val obj : JSONObject = JSONObject(response.body()?.string())
                    store.save(key, obj, {
                        kLog(TAG, "Saved $key to JsonStore")
                    })
                    return obj
                }
                catch (e : JSONException)
                {
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

            if(jsonLanguages == null)
            {
                jsonLanguages = parseAndSave(StoreId.LANGUAGES.name, backendManager.getAllLanguagesAsync().await())
            }
            if(jsonTranslations == null)
            {
                jsonTranslations = parseAndSave(StoreId.TRANSLATIONS.name, backendManager.getAllTranslationsAsync().await())
            }
            //kLog(TAG, "jsonLanguages = ${jsonLanguages?.toString(4) ?: "null"}")
            //kLog(TAG, "jsonTranslations = ${jsonTranslations?.toString(4) ?: "null"}")
        }
    }



    private fun parseAppOpenResponse(response: Response) {
        try {
            val obj : JSONObject = JSONObject(response.body()?.string())
            val data : JSONObject = obj.getJSONObject("data")
            // set current locale
            currentLocale = obj.getJSONObject("meta").getJSONObject("language").getString("locale")
            //kLog(TAG, "AppOpen response = ${data.toString(4)}")
            val translate : JSONObject? = data.getJSONObject("translate")
            if(translate != null)
                translationManager.parseTranslations(translate)
            if(data.has("update"))
                appUpdate = AppUpdate(data.getJSONObject("update"))
        }
        catch (e : JSONException)
        {
            e.printStackTrace()
        }
    }

}