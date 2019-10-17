package dk.nodes.nstack.kotlin

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import dk.nodes.nstack.kotlin.data.terms.TermsRepository
import dk.nodes.nstack.kotlin.features.common.ActiveActivityHolder
import dk.nodes.nstack.kotlin.features.mainmenu.presentation.MainMenuDisplayer
import dk.nodes.nstack.kotlin.managers.*
import dk.nodes.nstack.kotlin.models.*
import dk.nodes.nstack.kotlin.models.local.Environment
import dk.nodes.nstack.kotlin.plugin.NStackViewPlugin
import dk.nodes.nstack.kotlin.provider.TranslationHolder
import dk.nodes.nstack.kotlin.providers.ManagersModule
import dk.nodes.nstack.kotlin.providers.NStackModule
import dk.nodes.nstack.kotlin.providers.RepositoryModule
import dk.nodes.nstack.kotlin.util.*
import dk.nodes.nstack.kotlin.util.extensions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * NStack
 */
@SuppressLint("StaticFieldLeak", "LogNotTimber")
object NStack {

    // Has our app been started yet?
    private var isInitialized: Boolean = false

    // Variables
    var appIdKey: String = ""
        private set
    var appApiKey: String = ""
        private set
    var env: String = ""
        private set

    val appClientInfo: ClientAppInfo
        get() = appInfo

    val translationHolder = object : TranslationHolder {
        override fun hasKey(key: String?): Boolean {
            return this@NStack.hasKey(key)
        }

        override fun getTranslationByKey(key: String?): String? {
            return this@NStack.getTranslationByKey(key)
        }
    }

    private var currentLanguage: JSONObject? = null

    private var activeActivityHolder: ActiveActivityHolder? = null

    // Internally used classes
    private lateinit var classTranslationManager: ClassTranslationManager
    private lateinit var viewTranslationManager: ViewTranslationManager
    private lateinit var assetCacheManager: AssetCacheManager
    private lateinit var connectionManager: ConnectionManager
    private lateinit var appInfo: ClientAppInfo
    private lateinit var networkManager: NetworkManager
    private lateinit var appOpenSettingsManager: AppOpenSettingsManager
    private lateinit var prefManager: PrefManager
    private lateinit var contextWrapper: ContextWrapper
    private lateinit var mainMenuDisplayer: MainMenuDisplayer
    private lateinit var termsRepository: TermsRepository

    // Cache Maps
    private var networkLanguages: Map<Locale, JSONObject>? = null
    private var cacheLanguages: Map<Locale, JSONObject> = hashMapOf()

    private val handler: Handler = Handler()

    // Listener Lists
    private val onLanguageChangedList = mutableListOf<LanguageListener?>()
    private val onLanguagesChangedList = mutableListOf<LanguagesListener?>()
    private val plugins = mutableListOf<Any>()
    private val nstackViewPlugins: List<NStackViewPlugin>
        get() = plugins.filterIsInstance<NStackViewPlugin>()

    /**
     * Device Change Broadcast Listener
     *
     * Will listen for any changes in the device locale and send out the new locale for the user to do whatever they need to with
     */
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val configuration = context.resources.configuration
            val newLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getSystemLocale(configuration)
            } else {
                getSystemLocaleLegacy(configuration)
            }
            if (autoChangeLanguage) {
                language = newLocale
            }
        }
    }

    @SuppressWarnings("deprecation")
    private fun getSystemLocaleLegacy(config: Configuration): Locale {
        Build.VERSION.RELEASE
        return config.locale
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun getSystemLocale(config: Configuration): Locale {
        return config.locales.get(0)
    }

    /**
     * A map of all available languages keyed by their locale
     *
     * Will return the network languages if available if not it will return the
     * asset cache languages (Will typically be the oldest version of the language)
     */
    val languages: Map<Locale, JSONObject>
        get() {
            return if (networkLanguages == null || networkLanguages?.size == 0) {
                cacheLanguages
            } else {
                networkLanguages ?: cacheLanguages
            }
        }

    /**
     * Listener specifically for listening for any app update events
     */
    var onAppUpdateListener: ((AppUpdateData) -> Unit)? = null

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

    var baseUrl = "https://nstack.io"

    var defaultLanguage: Locale = Locale.UK
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
    val availableLanguages: ArrayList<Locale>
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
            NLog.level = value
        }

    /**
     * Enable/Disable versionUpdateTestMode
     */
    var versionUpdateTestMode: Boolean
        get() = appOpenSettingsManager.getAppOpenSettings().versionUpdateTestMode
        set(value) {
            appOpenSettingsManager.versionUpdateTestMode = value
        }


    /**
     * If flag is set to true this will auto change NStack's language when the device's locale is changed
     */
    var autoChangeLanguage: Boolean = false

    /**
     * Class Start
     */

    @Deprecated(
            "Use init sending debug mode",
            ReplaceWith("init(context, boolean)", "dk.nodes.nstack.kotlin.NStack.init")
    )
    fun init(context: Context) {
        init(context, false)
    }

    fun init(context: Context, debugMode: Boolean, vararg plugin: Any) {
        NLog.i(this, "NStack initializing")
        if (isInitialized) {
            NLog.w(this, "NStack already initialized")
            return
        }
        this.debugMode = debugMode

        val nstackModule = NStackModule(context, translationHolder)
        val managersModule = ManagersModule(nstackModule)
        val repositoryModule = RepositoryModule(nstackModule)

        val nstackMeta = nstackModule.provideNStackMeta()
        appIdKey = nstackMeta.appIdKey
        appApiKey = nstackMeta.apiKey
        env = nstackMeta.env

        viewTranslationManager = nstackModule.provideViewTranslationManager()
        classTranslationManager = nstackModule.provideClassTranslationManager()

        registerLocaleChangeBroadcastListener(context)

        plugins.addAll(plugin)
        viewTranslationManager = nstackModule.provideViewTranslationManager()
        appInfo = nstackModule.provideClientAppInfo()
        plugins += viewTranslationManager
        connectionManager = nstackModule.provideConnectionManager()
        assetCacheManager = managersModule.provideAssetCacheManager()
        appOpenSettingsManager = managersModule.provideAppOpenSettingsManager()
        prefManager = managersModule.providePrefManager()
        contextWrapper = nstackModule.provideContextWrapper()
        networkManager = nstackModule.provideNetworkManager()
        mainMenuDisplayer = createMainMenuDisplayer(context)

        termsRepository = repositoryModule.provideTermsRepository()

        loadCacheTranslations()

        this.activeActivityHolder = ActiveActivityHolder()
                .also { holder -> registerActiveActivityHolderToAppLifecycle(context, holder) }

        if (Environment(env).shouldEnableTestMode) {
            versionUpdateTestMode = true
        }

        isInitialized = true
    }

    private fun createMainMenuDisplayer(context: Context): MainMenuDisplayer {

        val liveEditManager = LiveEditManager(
                translationHolder,
                viewTranslationManager,
                networkManager,
                appOpenSettingsManager
        )

        return MainMenuDisplayer(liveEditManager)
    }

    private fun registerActiveActivityHolderToAppLifecycle(
            context: Context,
            activeActivityHolder: ActiveActivityHolder
    ) {
        val appContext = context
                .applicationContext as? Application
                ?: throw IllegalStateException("Could not get application context")

        appContext.registerActivityLifecycleCallbacks(activeActivityHolder)
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
        language = localeString.locale
    }

    /**
     * Callback method for when the app is first opened
     */
    fun appOpen(callback: AppOpenCallback = {}) {
        check(isInitialized) { "init() has not been called" }

        val localeString = language.toString()

        val appOpenSettings = appOpenSettingsManager.getAppOpenSettings()

        NLog.d(this, "onAppOpened -> $localeString $appOpenSettings")

        // If we aren't connected we should just send the app open call back as none
        if (!connectionManager.isConnected) {
            NLog.e(this, "No internet skipping appOpen")
            onAppUpdateListener?.invoke(AppUpdateData())
            return
        }

        networkManager
            .postAppOpen(appOpenSettings, localeString,
                { appUpdate ->
                    NLog.d(
                        this,
                        "NStack appOpen "
                    )

                    termsRepository.setLatestTerms(appUpdate.terms)

                    val appOpenCallbackCount = AppOpenCallbackCount(
                        callsCount = appUpdate.localize.count { it.shouldUpdate }
                    )

                    val languageFetchCallback = LanguageFetchCallback(appUpdate.localize.size)

                    appUpdate.localize.forEach { localizeIndex ->
                        if (localizeIndex.shouldUpdate) {
                            networkManager.loadTranslation(localizeIndex.url, {
                                prefManager.setTranslations(localizeIndex.language.locale, it)

                                try {
                                    networkLanguages = networkLanguages?.toMutableMap()?.apply {
                                        put(
                                            localizeIndex.language.locale,
                                            it.asJsonObject ?: return@apply
                                        )
                                    }?.toMap()
                                } catch (e: Exception) {
                                    NLog.e(this, e.toString())
                                }

                                if (localizeIndex.language.isBestFit) {
                                    onLanguagesChanged()
                                    onLanguageChanged()
                                }

                                appOpenCallbackCount.callsCount--
                                if (appOpenCallbackCount.callsCount <= 0 && !appOpenCallbackCount.callbackRan) {
                                    contextWrapper.runUiAction {
                                        callback(true)
                                        onAppUpdateListener?.invoke(appUpdate)
                                    }
                                    appOpenCallbackCount.callbackRan = true
                                }

                                languageFetchCallback.fetchedLanguage()
                            }, {
                                NLog.e(
                                    this,
                                    "Could not load translations for ${localizeIndex.language.locale}",
                                    it
                                )

                                appOpenCallbackCount.callsCount--
                                if (appOpenCallbackCount.callsCount <= 0 && !appOpenCallbackCount.callbackRan) {
                                    contextWrapper.runUiAction {
                                        callback.invoke(true)
                                        onAppUpdateListener?.invoke(appUpdate)
                                    }
                                    appOpenCallbackCount.callbackRan = true
                                }

                                languageFetchCallback.fetchedLanguage()
                            })

                            appOpenSettingsManager.setUpdateDate()
                        }
                        if (localizeIndex.language.isDefault) {
                            defaultLanguage = localizeIndex.language.locale
                        }

                        if (localizeIndex.language.isBestFit) {
                            language = localizeIndex.language.locale
                        }

                        if (appOpenCallbackCount.callsCount == 0 && !appOpenCallbackCount.callbackRan) {
                            contextWrapper.runUiAction {
                                callback.invoke(true)
                                onAppUpdateListener?.invoke(appUpdate)
                            }
                            appOpenCallbackCount.callbackRan = true
                        }
                    }

                    languageFetchCallback.done = {
                        contextWrapper.runUiAction {
                            callback.invoke(true)
                            onAppUpdateListener?.invoke(appUpdate)
                        }
                    }
                },
                {
                    NLog.e(this, "Error: onAppOpened", it)

                    // If our update failed for whatever reason we should still send an no update start
                    callback.invoke(false)
                    onAppUpdateListener?.invoke(AppUpdateData())
                }
            )
    }


    /**
     * Coroutine version of AppOpen: Callback method for when the app is first opened
     *
     */
    suspend fun appOpen(): AppOpenResult {
        check(isInitialized) { "init() has not been called" }

        val localeString = language.toString()
        val appOpenSettings = appOpenSettingsManager.getAppOpenSettings()

        NLog.d(this, "onAppOpened -> $localeString $appOpenSettings")

        // If we aren't connected we should just send the app open call back as none
        if (!connectionManager.isConnected) {
            NLog.e(this, "No internet skipping appOpen")
            return AppOpenResult.NoInternet
        }

        try {
            return when (val result = networkManager.postAppOpen(appOpenSettings, localeString)) {
                is AppOpenResult.Success -> {
                    NLog.d(this, "NStack appOpen")

                    termsRepository.setLatestTerms(result.appUpdateResponse.data.terms)
                    result.appUpdateResponse.data.localize.forEach { handleLocalizeIndex(it) }

                    val shouldUpdateTranslationClass =
                            result.appUpdateResponse.data.localize.any { it.shouldUpdate }
                    if (shouldUpdateTranslationClass) {
                        NLog.e(this, "ShouldUpdate is set, updating Translations class...")
                        withContext(Dispatchers.Main) {
                            onLanguagesChanged()
                            onLanguageChanged()
                        }
                    }

                    result
                }
                else -> {
                    NLog.e(this, "Error: onAppOpened")
                    result
                }
            }
        } catch (e: Exception) {
            NLog.e(this, "Error: onAppOpened - network request probably failed")
            return AppOpenResult.Failure
        }
    }

    private suspend fun handleLocalizeIndex(index: LocalizeIndex) {
        if (index.shouldUpdate) {
            val translation = networkManager.loadTranslation(index.url) ?: return
            prefManager.setTranslations(index.language.locale, translation)

            try {
                networkLanguages = networkLanguages?.toMutableMap()?.apply {
                    put(index.language.locale, translation.asJsonObject ?: return@apply)
                }
            } catch (e: Exception) {
                NLog.e(this, e.toString())
            }

            appOpenSettingsManager.setUpdateDate()
        }
        if (index.language.isDefault) {
            defaultLanguage = index.language.locale
        }
        if (index.language.isBestFit) {
            language = index.language.locale
        }
    }

    /**
     * Call it to notify that the message was seen and doesn't need to appear anymore
     */
    fun messageSeen(message: Message) {
        val appOpenSettings = appOpenSettingsManager.getAppOpenSettings()
        networkManager.postMessageSeen(appOpenSettings.guid, message.id)
    }

    /**
     * Call it to notify that the rate reminder was seen and doesn't need to appear any more
     * @param rated - true if user pressed Yes, false if user pressed No, not called if user pressed Later
     */
    @Deprecated("use RateReminder to check and show rate reminder")
    fun onRateReminderAction(rated: Boolean) {
        val appOpenSettings = appOpenSettingsManager.getAppOpenSettings()
        networkManager.postRateReminderSeen(appOpenSettings, rated)
    }

    /**
     * Call it to get a Response created in Collection in the given NStack application
     * @param slug - copy paste the text slug from the list of responses
     */
    fun getCollectionResponse(
            slug: String,
            onSuccess: (String) -> Unit,
            onError: (Exception) -> Unit
    ) {
        networkManager.getResponse(slug, onSuccess, onError)
    }

    /**
     * Call it to get a Response created in Collection in the given NStack application
     * This is the coroutine version for Kotlin
     * @param slug - copy paste the text slug from the list of responses
     */
    suspend fun getCollectionResponse(slug: String): String? {
        return networkManager.getResponseSync(slug)
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
        nstackViewPlugins.forEach { it.addView(WeakReference(view), translationData) }
    }

    private fun hasKey(key: String?): Boolean {
        return currentLanguage?.has(key?.cleanKeyName) ?: false
    }

    /**
     * Triggers a translation of all currently cached views
     */
    fun translate() {
        nstackViewPlugins.forEach { it.translate() }
    }

    /**
     * Clears all cached views
     */
    fun clearViewCache() {
        nstackViewPlugins.forEach { it.clear() }
    }

    /**
     * Call this method when you're done using the NStack Library
     */
    fun destroy(context: Context) {
        context.unregisterReceiver(broadcastReceiver)
    }

    private fun registerLocaleChangeBroadcastListener(context: Context) {
        val filter = IntentFilter(Intent.ACTION_LOCALE_CHANGED)
        context.registerReceiver(broadcastReceiver, filter)
    }

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
     * On State Change Listeners
     */
    private fun onLanguageChanged() {
        val languageByLocale = searchForLanguageByLocale(language)

        NLog.d(this, "On Language Changed: $currentLanguage")

        languageByLocale?.let {
            classTranslationManager.parseTranslations(it)
            onLanguageChanged(language)
            parseTranslations(it)
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
                    .asSequence()
                    // Do our languages match
                    .filter { it.languageCode == locale.languageCode }
                    // Find the value for that language
                    .map { languages[it] }
                    // Return the first value or null
                    .firstOrNull()
        }
    }

    /**
     * Run Ui Action
     */
    fun runUiAction(action: () -> Unit) {
        handler.post {
            action()
        }
    }

    /**
     * Listener Methods
     */

    // Listener

    fun addLanguageChangeListener(listener: OnLanguageChangedListener) {
        onLanguageChangedList.add(
                LanguageListener(
                        onLanguageChangedListener = listener
                )
        )
    }

    fun removeLanguageChangeListener(listener: OnLanguageChangedListener) {
        onLanguageChangedList.removeFirst { it.onLanguageChangedListener == listener }
    }

    // Function

    fun addLanguageChangeListener(listener: OnLanguageChangedFunction) {
        onLanguageChangedList.add(
                LanguageListener(
                        onLanguageChangedFunction = listener
                )
        )
    }

    fun removeLanguageChangeListener(listener: OnLanguageChangedFunction) {
        onLanguageChangedList.removeFirst { it.onLanguageChangedFunction == listener }
    }

    // Languages Listeners

    // Listener

    fun addLanguagesChangeListener(listener: OnLanguagesChangedListener) {
        onLanguagesChangedList.add(
                LanguagesListener(
                        onLanguagesChangedListener = listener
                )
        )
    }

    fun removeLanguagesChangeListener(listener: OnLanguagesChangedListener) {
        onLanguagesChangedList.removeFirst { it.onLanguagesChangedListener == listener }
    }

    // Function

    fun addLanguagesChangeListener(listener: OnLanguagesChangedFunction) {
        onLanguagesChangedList.add(
                LanguagesListener(
                        onLanguagesChangedFunction = listener
                )
        )
    }

    fun removeLanguagesChangeListener(listener: OnLanguagesChangedFunction) {
        onLanguagesChangedList.removeFirst { it.onLanguagesChangedFunction == listener }
    }

    /**
     * Exposed Adders(?)
     */

    private fun getTranslationByKey(key: String?): String? {
        return currentLanguage?.optString(key?.cleanKeyName ?: return null, null)
    }

    fun addView(view: View, translationData: TranslationData) {
        nstackViewPlugins.forEach { it.addView(WeakReference(view), translationData) }
    }

    fun hasKey(@StringRes resId: Int, context: Context): Boolean {
        return hasKey(context.getString(resId))
    }

    fun getTranslation(@StringRes resId: Int, context: Context): String? {
        return getTranslationByKey(context.getString(resId))
    }

    private fun parseTranslations(jsonParent: JSONObject) {
        // Clear our language map
        currentLanguage = JSONObject()

        // Pull our keys
        val keys = jsonParent.keys()

        // Iterate through each key and add the sub section
        keys.forEach { sectionName ->
            val subSection: JSONObject? = jsonParent.optJSONObject(sectionName)

            if (subSection != null) {
                parseSubsection(sectionName, subSection)
            }
        }
    }

    /**
     * Goes through each sub section and adds the value under the new key
     */

    private fun parseSubsection(sectionName: String, jsonSection: JSONObject) {
        jsonSection.keys().forEach {
            currentLanguage?.put("${sectionName}_$it", jsonSection.getString(it))
        }
    }

    /**
     * Enables the live editing feature for text in the UI
     *
     * Please note that the recent versions of NStack support additional features triggered with the
     * shake gesture so this method is now deprecated and just calls [enableMenuOnShake]
     *
     * @see enableMenuOnShake
     */
    @Deprecated(
            "Deprecated to support more features on shake.",
            ReplaceWith(
                    "enableMenuOnShake(context)",
                    "dk.nodes.nstack.kotlin.NStack.enableMenuOnShake"
            )
    )
    fun enableLiveEdit(context: Context) = enableMenuOnShake(context)

    fun enableMenuOnShake(context: Context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val shakeDetector = ShakeDetector(object : ShakeDetector.Listener {
            override fun hearShake() {
                val activity = activeActivityHolder?.foregroundActivity ?: return
                mainMenuDisplayer.trigger(activity)
            }
        })

        shakeDetector.start(sensorManager)
    }

    object RateReminder {

        var title: String = "_rate reminder"
        var message: String = "_rate reminder message"
        var yesButton: String = "_yes"
        var noButton: String = "_no"
        var skipButton: String = "_later"

        private val settings: AppOpenSettings by lazy { appOpenSettingsManager.getAppOpenSettings() }

        private var rateReminderId: Int = 0

        /**
         * Call it in order to check whether the app should show rate reminder dialog
         *
         * if rate reminder should be shown call RateReminder#show in order to show it
         *
         * @return true if rate reminder dialog should be shown
         */
        suspend fun shouldShow(): Boolean {
            return networkManager.getRateReminder2(settings)?.also {
                rateReminderId = it.id
            } != null
        }

        /**
         * call this when user performs a rate reminder related action
         *
         * @param action - user performed rate reminder related action
         *                  for convenience nstack gradle plugin generates enum RateReminderAction
         *                  which contains actions' names
         */
        suspend fun action(action: String) {
            networkManager.postRateReminderAction(
                    appOpenSettingsManager.getAppOpenSettings(),
                    action
            )
        }

        /**
         * Shows an alert dialog asking user whether they would rate the app
         *
         * title, message, yesButton, noButton, skipButton should be set before calling this method
         *
         * @param context change appearance of the dialog by passing ContextThemeWrapper
         * @throws IllegalStateException if RateReminder#show wasn't called or it returned false
         * @return RateReminderAnswer, when answer is
         *         POSITIVE - app should take user to the play store
         *         NEGATIVE - app should take user to the feedback screen
         *         SKIP - nothing for the app to do
         */
        suspend fun show(context: Context): RateReminderAnswer {
            check(rateReminderId != 0) { "check rate reminder with shouldShow before showing the dialog" }
            val answer = suspendCoroutine<RateReminderAnswer> {
                AlertDialog.Builder(context)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(yesButton) { _, _ ->
                            it.resume(RateReminderAnswer.POSITIVE)
                        }
                        .setNegativeButton(noButton) { _, _ ->
                            it.resume(RateReminderAnswer.NEGATIVE)
                        }
                        .setNeutralButton(skipButton) { _, _ ->
                            it.resume(RateReminderAnswer.SKIP)
                        }
                        .setCancelable(false)
                        .show()
            }
            withContext(Dispatchers.IO) {
                networkManager.postRateReminderAction(settings, rateReminderId, answer.apiName)
            }
            rateReminderId = 0
            return answer
        }
    }

    object Feedback {

        var appVersion: String = ""
        var deviceName: String = ""
        var name: String = ""
        var email: String = ""

        suspend fun send(
                message: String = ""
        ) {
            val feedback = Feedback(
                    appVersion,
                    deviceName,
                    name,
                    email,
                    message
            )
            networkManager.postFeedback(feedback)
        }
    }

    object Terms {

        /**
         * A list of terms which are not yet accepted by this app instance (GUID)
         *
         * This is a local copy of terms provided via [AppOpenResult]
         */
        val latestTerms = termsRepository.getLatestTerms().filter { it.version != null }

        /**
         * Provides latest [TermsDetails] for given [termsID]
         */
        fun getTermsDetails(termsID: Long,
                            onSuccess: (TermsDetails) -> Unit,
                            onError: (Exception) -> Unit) {
            networkManager.getLatestTerms(
                    termsID = termsID,
                    acceptLanguage = language.toString(),
                    settings = appOpenSettingsManager.getAppOpenSettings(),
                    onSuccess = { runUiAction { onSuccess(it) }},
                    onError = { runUiAction { onError(it) }}
            )
        }

        /**
         * Set a version of terms to viewed by this app instance (GUID)
         */
        fun setTermsViewed(versionID : Long,
                           userID : String,
                           onSuccess: () -> Unit,
                           onError: (Exception) -> Unit) {
            networkManager.setTermsViewed(
                    versionID = versionID,
                    userID = userID,
                    locale = language.toString().replace("_", "-"),
                    settings = appOpenSettingsManager.getAppOpenSettings(),
                    onSuccess = { runUiAction { onSuccess() }},
                    onError = { runUiAction { onError(it) }}
            )
        }
    }
}
