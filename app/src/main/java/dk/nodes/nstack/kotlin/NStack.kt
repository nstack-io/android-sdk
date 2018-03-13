package dk.nodes.nstack.kotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import com.google.gson.JsonObject
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.managers.TranslationManager
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.util.AppOpenCallback
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.VersionControlCallback
import dk.nodes.nstack.kotlin.util.toLanguageMap
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object NStack {
    private val TAG = "NStack"
    // Has our app been started yet?
    private var isInitialized: Boolean = false

    // Variables
    private var appIdKey: String = ""
    private var appApiKey: String = ""

    // Internally used classes
    private var translationManager: TranslationManager = TranslationManager()
    private lateinit var assetCacheManager: AssetCacheManager
    private lateinit var clientAppInfo: ClientAppInfo
    private lateinit var networkManager: NetworkManager
    private lateinit var prefManager: PrefManager

    // Cache Maps
    private var networkLanguages: HashMap<Locale, JsonObject>? = null
    private var cacheLanguages: HashMap<Locale, JsonObject> = hashMapOf()

    // Internal Variables
    private var refreshPeriod: Long = TimeUnit.HOURS.toMillis(1)

    // Method for determining whether to use a
    var languages: HashMap<Locale, JsonObject>
        set(value) {
        }
        get() {
            return if (networkLanguages == null || networkLanguages?.size == 0) {
                cacheLanguages
            } else {
                networkLanguages ?: cacheLanguages
            }
        }


    // Listener Lists
    private var onLanguageChangedList: ArrayList<((Locale) -> Unit)?> = arrayListOf()
    private var onLanguagesChangedList: ArrayList<(() -> Unit)?> = arrayListOf()

    // States
    var translationClass: Class<*>?
        set(value) {
            TranslationManager.translationClass = value
        }
        get() {
            return TranslationManager.translationClass
        }
    var customRequestUrl: String?
        set(value) {
            networkManager.customRequestUrl = value
        }
        get() {
            return networkManager.customRequestUrl
        }
    var language: Locale = Locale.getDefault()
        set(value) {
            field = value
            onLanguageChanged()
        }
    var availableLanguages: ArrayList<Locale> = arrayListOf()
        get() {
            return ArrayList(languages.keys)
        }
    var debugMode: Boolean = false
        set(value) {
            field = value
            onDebugModeChanged()
        }

    /**
     * Class Start
     */

    fun init(context: Context) {
        NLog.i(TAG, "NStack initializing")

        if (isInitialized) {
            NLog.w(TAG, "NStack already initialized")
            return
        }

        getApplicationInfo(context)

        networkManager = NetworkManager(context)
        assetCacheManager = AssetCacheManager(context)
        clientAppInfo = ClientAppInfo(context)
        prefManager = PrefManager(context)

        loadCacheTranslations()
        loadNetworkTranslations()
    }

    /**
     *  Allows the user to set the language via string
     *  Should match the format below
     *  en_GB
     *  en-GB
     *
     *  This method will only care about the first front letters
     */
    fun setLanguageByString(string: String) {
        var languageString = when {
            string.contains("_") -> string.split("_").first()
            string.contains("-") -> string.split("-").first()
            else                 -> string
        }

        languageString = languageString.toLowerCase()

        languages.keys
                .filter { it.language.toLowerCase() == languageString }
                .forEach {
                    language = it
                }
    }

    /**
     * Callback method for when the app is first opened
     */
    fun onAppOpened(callback: AppOpenCallback = {}) {
        if (!isInitialized) {
            throw IllegalStateException("init() has not been called")
        }

        //TODO implement
    }

    /**
     * Callback method for checking whether the app is up to date
     */
    fun versionControl(activity: Activity, versionControlCallback: VersionControlCallback = {}) {
        if (!isInitialized) {
            throw IllegalStateException("init() has not been called")
        }

        //TODO implement
    }

    fun setRefreshPeriod(duration: Long, timeUnit: TimeUnit) {
        this.refreshPeriod = timeUnit.toMillis(duration)
    }

    /**
     * Gets the app ID & Api Key using the app context to access the manifest
     */

    private fun getApplicationInfo(context: Context) {
        NLog.i(TAG, "getApplicationInfo")

        val applicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
        )

        val applicationMetaData = applicationInfo.metaData

        if (applicationMetaData.containsKey("dk.nodes.nstack.appId")) {
            appIdKey = applicationMetaData?.getString("dk.nodes.nstack.appId") ?: ""
        }

        if (applicationMetaData.containsKey("dk.nodes.nstack.apiKey")) {
            appApiKey = applicationMetaData?.getString("dk.nodes.nstack.apiKey") ?: ""
        }

        if (appIdKey.isEmpty()) {
            NLog.e(TAG, "Missing dk.nodes.nstack.appId-> disabling network")
        }

        if (appApiKey.isEmpty()) {
            NLog.e(TAG, "Missing dk.nodes.nstack.apiKey -> disabling network")
        }

    }

    /**
     * Loaders
     */

    /**
     * Loads our languages from the asset cache
     */

    private fun loadCacheTranslations() {
        cacheLanguages = prefManager.getTranslations()

        if (cacheLanguages.size == 0) {
            NLog.i(TAG, "Missing Preference Cache -> Loading from assets")
            cacheLanguages = assetCacheManager.loadTranslations()
        }

        onLanguagesChanged()
    }

    /**
     * Loads our languages from the network¬
     */

    private fun loadNetworkTranslations() {
        val hasRequireTimePassed = hasRequireTimePassed()

        if (!hasRequireTimePassed && !debugMode) {
            NLog.i(TAG, "Skipping Network Call")
            return
        }

        networkManager
                .loadTranslations()
                .subscribe(
                        {
                            NLog.i(TAG, "Successfully Loaded Network Translations")

                            prefManager.setTranslations(it)

                            networkLanguages = it.toLanguageMap()

                            onLanguagesChanged()
                        },
                        {
                            NLog.i(TAG, "Error Loading Network Translations")
                            networkLanguages = null
                            it.printStackTrace()
                        }
                )
    }

    private fun hasRequireTimePassed(): Boolean {
        val nowDate = Date()
        val lastUpdateDate = prefManager.getLastUpdateDate() ?: return true

        val nowTimeStamp = nowDate.time
        val lastTimeStamp = lastUpdateDate.time

        NLog.d(TAG, "Now Date -> $nowTimeStamp Last Update -> $lastTimeStamp")

        val passedTime = nowTimeStamp - lastTimeStamp

        NLog.d(TAG, "Refresh Period: $refreshPeriod")
        NLog.d(TAG, "Passed Time: $passedTime")

        val hasTimePassed = passedTime >= refreshPeriod

        NLog.d(TAG, "Has Time Passed: $hasTimePassed")

        return hasTimePassed
    }

    /**
     * On State Change Listeners
     */

    private fun onLanguageChanged() {
        val selectedLanguage = languages[language] ?: return
        translationManager.parseTranslations(selectedLanguage)
        onLanguageChanged(language)
    }

    private fun onDebugModeChanged() {
        NLog.enableLogging = debugMode
    }

    /**
     * Listener Methods
     */

    private fun onLanguageChanged(locale: Locale) {
        onLanguageChangedList.forEach {
            it?.invoke(locale)
        }
    }

    private fun onLanguagesChanged() {
        onLanguagesChangedList.forEach {
            it?.invoke()
        }
    }

    fun addLanguageChangeListener(listener: (Locale) -> Unit) {
        onLanguageChangedList.add(listener)
    }

    fun removeLanguageChangeListener(listener: (Locale) -> Unit) {
        onLanguageChangedList.remove(listener)
    }

    fun addLanguagesChangeListener(listener: () -> Unit) {
        onLanguagesChangedList.add(listener)
    }

    fun removeLanguagesChangeListener(listener: () -> Unit) {
        onLanguagesChangedList.remove(listener)
    }

    /**
     * Exposed Getters
     */

    fun getAppClientInfo(): ClientAppInfo {
        return clientAppInfo
    }

    fun getAppIdKey(): String {
        return appIdKey
    }

    fun getAppApiKey(): String {
        return appApiKey
    }
}