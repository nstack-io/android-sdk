package dk.nodes.nstack.kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import dk.nodes.nstack.kotlin.appopen.AppOpenSettings
import dk.nodes.nstack.kotlin.appopen.AppUpdate
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.managers.TranslationManager
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.util.AppOpenCallback
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.toLanguageMap
import dk.nodes.nstack.kotlin.util.toLocale
import org.json.JSONObject
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
    private lateinit var appOpenSettings: AppOpenSettings
    private lateinit var assetCacheManager: AssetCacheManager
    private lateinit var clientAppInfo: ClientAppInfo
    private lateinit var networkManager: NetworkManager
    private lateinit var prefManager: PrefManager

    // Cache Maps
    private var networkLanguages: HashMap<Locale, JSONObject>? = null
    private var cacheLanguages: HashMap<Locale, JSONObject> = hashMapOf()

    // Internal Variables
    private var appUpdate: AppUpdate? = null
    private var refreshPeriod: Long = TimeUnit.HOURS.toMillis(1)

    /**
     * A map of all available languages keyed by their locale
     *
     * Will return the network languages if available if not it will return the
     * asset cache languages (Will typically be the oldest version of the language)
     */
    var languages: HashMap<Locale, JSONObject>
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
    /**
     * Listener specifically for listening for any app update events
     */
    var onAppUpdateListener: ((AppUpdate) -> Unit)? = null
        set(value) {
            field = value
            onAppUpdateListenerChanged()
        }

    // States
    /**
     * Set the class for our translation class
     */
    var translationClass: Class<*>?
        set(value) {
            TranslationManager.translationClass = value
        }
        get() {
            return TranslationManager.translationClass
        }
    /**
     * Custom Request URL
     * Used for settings a custom end point for us to pull our NStack Translations from
     */
    var customRequestUrl: String? = null

    /**
     * Used for settings or getting the current locale selected for language
     */
    var language: Locale = Locale.getDefault()
        set(value) {
            field = value
            onLanguageChanged()
        }
    /**
     * Return a list of all available languages
     */
    var availableLanguages: ArrayList<Locale> = arrayListOf()
        get() {
            return ArrayList(languages.keys)
        }
    /**
     * Enable/Disable debug mode
     */
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

        if (!checkForValidKeys()) {
            throw IllegalStateException("Missing app keys in manifest")
        }

        networkManager = NetworkManager(context)
        assetCacheManager = AssetCacheManager(context)
        clientAppInfo = ClientAppInfo(context)
        prefManager = PrefManager(context)
        appOpenSettings = AppOpenSettings(context)

        loadCacheTranslations()
        loadNetworkTranslations()

        isInitialized = true
    }

    /**
     *  Allows the user to set the language via string
     *  Should match the format below
     *  en_GB
     *  en-GB
     *
     *  This method will only care about the first front letters
     */
    fun setLanguageByString(localeString: String) {
        val locale = localeString.toLocale()
        language = locale
    }

    /**
     * Callback method for when the app is first opened
     */

    fun appOpen(callback: AppOpenCallback = {}) {
        if (!isInitialized) {
            throw IllegalStateException("init() has not been called")
        }

        val localeString = language.toString()

        NLog.d(TAG, "onAppOpened -> $localeString")

        networkManager
                .postAppOpen(appOpenSettings, localeString)
                .subscribe(
                        {
                            NLog.d(TAG, "Successful: onAppOpened -> $it")
                            callback.invoke(true)
                            appUpdate = it
                            onAppUpdateListener?.invoke(it)
                        },
                        {
                            NLog.e(TAG, "Error: onAppOpened")
                            it.printStackTrace()
                            callback.invoke(false)
                        }
                )
    }

    fun setRefreshPeriod(duration: Long, timeUnit: TimeUnit) {
        this.refreshPeriod = timeUnit.toMillis(duration)
    }


    /**
     * Method used to check for weather there were valid keys in the manifest or not
     */
    private fun checkForValidKeys(): Boolean {
        if (appIdKey.isEmpty()) {
            return false
        }

        if (appApiKey.isEmpty()) {
            return false
        }

        return true
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
        val selectedLanguage = searchForLanguageByLocale(language)

        selectedLanguage?.let {
            translationManager.parseTranslations(it)
            onLanguageChanged(language)
        }
    }

    private fun onAppUpdateListenerChanged() {
        appUpdate?.let {
            onAppUpdateListener?.invoke(it)
        }
    }

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

    private fun onDebugModeChanged() {
        NLog.enableLogging = debugMode
    }

    /**
     * Helper Methods
     */

    /**
     * Searches the available languages for any language matching the provided locale
     *
     * Provides both a perfect match or a fallback language and a default language
     */
    private fun searchForLanguageByLocale(locale: Locale): JSONObject? {
        var languageObject: JSONObject?
        // TODO determine how to get a default language from nStack
        // If we don't have one single language available just return null
        val defaultLanguage = availableLanguages.firstOrNull() ?: return null

        languageObject = if (languages.containsKey(language)) {
            languages[language]
        } else {
            // Search our available languages for any keys that might match
            availableLanguages
                    // Do our languages match
                    .filter { it.language == locale.language }
                    // Find the value for that language
                    .map { languages[it] }
                    // Return the first value or null
                    .firstOrNull()
        }

        // If after our search we still don't have a language then we should just default to our default
        if (languageObject == null) {
            languageObject = languages[defaultLanguage]
        }

        return languageObject
    }

    /**
     * Listener Methods
     */

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