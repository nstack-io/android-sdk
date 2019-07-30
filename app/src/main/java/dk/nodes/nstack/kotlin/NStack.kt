package dk.nodes.nstack.kotlin

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StringRes
import dk.nodes.nstack.kotlin.managers.*
import dk.nodes.nstack.kotlin.models.AppUpdate
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.util.*
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak", "LogNotTimber")
object NStack {
    // Has our app been started yet?
    private var isInitialized: Boolean = false

    // Variables
    private var appIdKey: String = ""
    private var appApiKey: String = ""

    // Internally used classes
    private var classTranslationManager = ClassTranslationManager()
    private var viewTranslationManager = ViewTranslationManager()
    private lateinit var assetCacheManager: AssetCacheManager
    private lateinit var connectionManager: ConnectionManager
    private lateinit var clientAppInfo: ClientAppInfo
    private lateinit var networkManager: NetworkManager
    private lateinit var appOpenSettingsManager: AppOpenSettingsManager
    private lateinit var prefManager: PrefManager

    // Cache Maps
    private var networkLanguages: HashMap<Locale, JSONObject>? = null
    private var cacheLanguages: HashMap<Locale, JSONObject> = hashMapOf()

    // Internal Variables
    private var refreshPeriod: Long = TimeUnit.HOURS.toMillis(1)

    private var handler: Handler = Handler()

    /**
     * Device Change Broadcast Listener
     *
     * Will listen for any changes in the device locale and send out the new locale for the user to do whatever they need to with
     */
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val newLocale = context.resources.configuration.locale
            if (autoChangeLanguage) {
                language = newLocale
            }
        }
    }

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
    private var onLanguageChangedList: ArrayList<LanguageListener?> = arrayListOf()
    private var onLanguagesChangedList: ArrayList<LanguagesListener?> = arrayListOf()

    /**
     * Listener specifically for listening for any app update events
     */
    var onAppUpdateListener: ((AppUpdate) -> Unit)? = null

    // States
    /**
     * Set the class for our translation class
     */
    var translationClass: Class<*>?
        set(value) {
            ClassTranslationManager.translationClass = value
        }
        get() {
            return ClassTranslationManager.translationClass
        }
    /**
     * Custom Request URL
     * Used for settings a custom end point for us to pull our NStack Translations from
     */
    var customRequestUrl: String? = null

    var defaultLanguage: Locale = Locale.US
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

    var skipNetworkLoading: Boolean = false
    /**
     * Enable/Disable debug mode
     */
    var debugMode: Boolean
        get() = NLog.enableLogging
        set(value) {
            NLog.enableLogging = value
        }
    /**
     * Set the level at which the debug log should output
     */
    var debugLogLevel: NLog.Level
        get() {
            return NLog.level
        }
        set(value) {
            NLog.level
        }

    /**
     * Enable/Disable live editing
     */
    var liveEditEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                viewTranslationManager.enableLiveEdit()
            } else {
                viewTranslationManager.disableLiveEdit()
            }
        }

    /**
     * If flag is set to true this will auto change NStack's language when the device's locale is changed
     */
    var autoChangeLanguage: Boolean = false

    /**
     * Class Start
     */

    fun init(context: Context) {
        NLog.i(this, "NStack initializing")

        if (isInitialized) {
            NLog.w(this, "NStack already initialized")
            return
        }

        getApplicationInfo(context)
        registerLocaleChangeBroadcastListener(context)

        networkManager = NetworkManager(context)
        connectionManager = ConnectionManager(context)
        assetCacheManager = AssetCacheManager(context)
        clientAppInfo = ClientAppInfo(context)
        appOpenSettingsManager = AppOpenSettingsManager(context)
        prefManager = PrefManager(context)

        loadCacheTranslations()

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

        val appOpenSettings = appOpenSettingsManager.getAppOpenSettings()

        NLog.d(this, "onAppOpened -> $localeString $appOpenSettings")

        // If we aren't connected we should just send the app open call back as none
        if (!connectionManager.isConnected()) {
            NLog.e(this, "No internet skipping appOpen")
            onAppUpdateListener?.invoke(AppUpdate())
            return
        }

        networkManager
                .postAppOpen(appOpenSettings, localeString,
                        {
                            NLog.d(
                                    this,
                                    "NStack appOpen -> translation updated: ${it.translationsUpdated}"
                            )

                            if (it.translationsUpdated) {
                                loadNetworkTranslations()
                            }

                            runUiAction {
                                callback.invoke(true)
                                onAppUpdateListener?.invoke(it)
                            }
                        },
                        {
                            NLog.e(this, "Error: onAppOpened", it)

                            // If our update failed for whatever reason we should still send an no update start
                            callback.invoke(false)
                            onAppUpdateListener?.invoke(AppUpdate())
                        }
                )
    }

    fun setRefreshPeriod(duration: Long, timeUnit: TimeUnit) {
        this.refreshPeriod = timeUnit.toMillis(duration)
    }

    /**
     * Triggers translation and add view to cached views
     */
    fun setTranslation(
            view: View,
            nstackKey: String,
            hint: String? = null,
            description: String? = null,
            textOn: String? = null,
            textOff: String? = null,
            contentDescription: String? = null,
            title: String? = null,
            subtitle: String? = null
    ) {
        if (!hasKey(nstackKey)) return

        val translationData = TranslationData(
                key = nstackKey,
                hint = hint,
                description = description,
                textOn = textOn,
                textOff = textOff,
                contentDescription = contentDescription,
                title = title,
                subtitle = subtitle
        )
        viewTranslationManager.addView(WeakReference(view), translationData)
    }

    fun hasKey(nstackKey: String): Boolean {
        return viewTranslationManager.hasKey(nstackKey)
    }

    /**
     * Triggers a translation of all currently cached views
     */

    fun translate() {
        viewTranslationManager.translate()
    }

    /**
     * Clears all cached views
     */

    fun clearViewCache() {
        viewTranslationManager.clear()
    }

    /**
     * Call this method when you're done using the NStack Library
     */
    fun destroy(context: Context) {
        context.unregisterReceiver(broadcastReceiver)
    }

    /**
     * Gets the app ID & Api Key using the app context to access the manifest
     */

    private fun getApplicationInfo(context: Context) {
        NLog.i(this, "getApplicationInfo")

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
            NLog.e(this, "Missing dk.nodes.nstack.appId")
        }

        if (appApiKey.isEmpty()) {
            NLog.e(this, "Missing dk.nodes.nstack.apiKey")
        }
    }

    private fun registerLocaleChangeBroadcastListener(context: Context) {
        val filter = IntentFilter(Intent.ACTION_LOCALE_CHANGED)
        context.registerReceiver(broadcastReceiver, filter)
    }

    /**
     * Loaders
     */

    /**
     * Loads our languages from the asset cache
     */

    private fun loadCacheTranslations() {
        NLog.e(this, "loadCacheTranslations")

        // Load our network cached data
        networkLanguages = prefManager.getTranslations()

        // Load our asset cached data
        cacheLanguages = assetCacheManager.loadTranslations()

        // Broadcast our languages changed
        onLanguagesChanged()

        // Broadcast our default language changed
        onLanguageChanged()
    }

    /**
     * Loads our languages from the network¬
     */

    private fun loadNetworkTranslations() {
        // If we aren't connected we shouldn't try polling for new data
        if (!connectionManager.isConnected()) {
            NLog.e(this, "Missing Network Connection")
            return
        }

        networkManager.loadTranslations(
                {
                    NLog.i(
                            this,
                            "Successfully Loaded Network Translations"
                    )

                    NLog.i(this, "Saving when we updated translations at -> ${Date()}")
                    //appOpenSettingsManager.setUpdateDate()

                    runUiAction {
                        prefManager.setTranslations(it)
                        networkLanguages = it.toLanguageMap()
                        onLanguageChanged()
                        onLanguagesChanged()
                    }
                },
                {
                    NLog.e(this, "Error Loading Network Translations", it)
                }
        )
    }

    /**
     * On State Change Listeners
     */

    private fun onLanguageChanged() {
        val selectedLanguage = searchForLanguageByLocale(language)

        NLog.d(this, "On Language Changed: $selectedLanguage")

        selectedLanguage?.let {
            viewTranslationManager.parseTranslations(it)
            classTranslationManager.parseTranslations(it)
            onLanguageChanged(language)
        }
    }

    private fun onLanguageChanged(locale: Locale) {
        onLanguageChangedList.forEach {
            it?.onLanguageChangedListener?.onLanguageChanged(locale)
            it?.onLanguageChangedFunction?.invoke(locale)
        }
    }

    private fun onLanguagesChanged() {
        onLanguagesChangedList.forEach {
            it?.onLanguagesChangedListener?.onLanguagesChanged()
            it?.onLanguagesChangedFunction?.invoke()
        }
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
        NLog.d(this, "searchForLanguageByLocale: $locale")

        // Search for our exact language
        var languageJson = searchForLocale(locale)

        // If that fails then we search for the default language
        if (languageJson == null && defaultLanguage != locale) {
            NLog.w(this, "Locating language failed $locale, Trying default $defaultLanguage")
            languageJson = searchForLocale(defaultLanguage)
            language = defaultLanguage
        }

        // And if all else fails then we just pick the first available language and hope for the best
        if (languageJson == null) {
            val firstAvailableLanguage = availableLanguages.firstOrNull()

            NLog.w(this, "Locating Default language failed $defaultLanguage")
            NLog.w(this, "Trying first available language $firstAvailableLanguage")

            firstAvailableLanguage?.let {
                languageJson = searchForLocale(it)
                language = firstAvailableLanguage
            }
        }

        return languageJson
    }

    private fun searchForLocale(locale: Locale): JSONObject? {
        return if (languages.containsKey(language)) {
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
    }

    /**
     * Run Ui Action
     */

    private fun runUiAction(action: () -> Unit) {
        handler.post {
            action()
        }
    }

    /**
     * Listener Methods
     */

    // Listener

    fun addLanguageChangeListener(listener: OnLanguageChangedListener) {
        onLanguageChangedList.add(LanguageListener(onLanguageChangedListener = listener))
    }

    fun removeLanguageChangeListener(listener: OnLanguageChangedListener) {
        val listenerContainer = onLanguageChangedList.firstOrNull { it?.onLanguageChangedListener == listener }
                ?: return
        onLanguageChangedList.remove(listenerContainer)
    }

    // Function

    fun addLanguageChangeListener(listener: OnLanguageChangedFunction) {
        onLanguageChangedList.add(LanguageListener(onLanguageChangedFunction = listener))
    }

    fun removeLanguageChangeListener(listener: OnLanguageChangedFunction) {
        val listenerContainer = onLanguageChangedList.firstOrNull { it?.onLanguageChangedFunction == listener }
                ?: return
        onLanguageChangedList.remove(listenerContainer)
    }

    // Languages Listeners

    // Listener

    fun addLanguagesChangeListener(listener: OnLanguagesChangedListener) {
        onLanguagesChangedList.add(LanguagesListener(onLanguagesChangedListener = listener))
    }

    fun removeLanguagesChangeListener(listener: OnLanguagesChangedListener) {
        val listenerContainer = onLanguagesChangedList.firstOrNull { it?.onLanguagesChangedListener == listener }
                ?: return
        onLanguagesChangedList.remove(listenerContainer)
    }

    //Function

    fun addLanguagesChangeListener(listener: OnLanguagesChangedFunction) {
        onLanguagesChangedList.add(LanguagesListener(onLanguagesChangedFunction = listener))
    }

    fun removeLanguagesChangeListener(listener: OnLanguagesChangedFunction) {
        val listenerContainer = onLanguagesChangedList.firstOrNull { it?.onLanguagesChangedFunction == listener }
                ?: return
        onLanguagesChangedList.remove(listenerContainer)
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

    /**
     * Exposed Adders(?)
     */

    fun getTranslationFromKey(key: String): String? {
        return viewTranslationManager.getTranslationByKey(key)
    }

    fun addView(view: View, translationData: TranslationData) {
        viewTranslationManager.addView(WeakReference(view), translationData)
    }

    fun hasKey(@StringRes resId: Int, context: Context) : Boolean{
        return hasKey(context.getString(resId))
    }

    fun getTranslation(@StringRes resId: Int, context: Context): String? {
        return getTranslationFromKey(context.getString(resId))
    }
}