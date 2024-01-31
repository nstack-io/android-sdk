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
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import dk.nodes.nstack.kotlin.NStack.Messages.show
import dk.nodes.nstack.kotlin.features.common.ActiveActivityHolder
import dk.nodes.nstack.kotlin.features.feedback.domain.model.ImageData
import dk.nodes.nstack.kotlin.features.feedback.presentation.FeedbackActivity
import dk.nodes.nstack.kotlin.features.mainmenu.presentation.MainMenuDisplayer
import dk.nodes.nstack.kotlin.features.messages.presentation.MessageDialog
import dk.nodes.nstack.kotlin.features.terms.data.TermsRepository
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.ClassTranslationManager
import dk.nodes.nstack.kotlin.managers.ConnectionManager
import dk.nodes.nstack.kotlin.managers.LiveEditManager
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.managers.ViewTranslationManager
import dk.nodes.nstack.kotlin.models.AppOpen
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.models.Error
import dk.nodes.nstack.kotlin.models.FeedbackType
import dk.nodes.nstack.kotlin.models.Message
import dk.nodes.nstack.kotlin.models.RateReminderAnswer
import dk.nodes.nstack.kotlin.models.Result
import dk.nodes.nstack.kotlin.models.TermsDetails
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.models.local.Environment
import dk.nodes.nstack.kotlin.plugin.NStackViewPlugin
import dk.nodes.nstack.kotlin.provider.TranslationHolder
import dk.nodes.nstack.kotlin.provider.gsonModule
import dk.nodes.nstack.kotlin.provider.httpClientModule
import dk.nodes.nstack.kotlin.providers.NStackKoinComponent
import dk.nodes.nstack.kotlin.providers.managersModule
import dk.nodes.nstack.kotlin.providers.nStackModule
import dk.nodes.nstack.kotlin.providers.repositoryModule
import dk.nodes.nstack.kotlin.providers.useCaseModule
import dk.nodes.nstack.kotlin.util.LanguageListener
import dk.nodes.nstack.kotlin.util.LanguagesListener
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.OnLanguageChangedFunction
import dk.nodes.nstack.kotlin.util.OnLanguageChangedListener
import dk.nodes.nstack.kotlin.util.OnLanguagesChangedFunction
import dk.nodes.nstack.kotlin.util.OnLanguagesChangedListener
import dk.nodes.nstack.kotlin.util.ShakeDetector
import dk.nodes.nstack.kotlin.util.extensions.ContextWrapper
import dk.nodes.nstack.kotlin.util.extensions.cleanKeyName
import dk.nodes.nstack.kotlin.util.extensions.consumable
import dk.nodes.nstack.kotlin.util.extensions.languageCode
import dk.nodes.nstack.kotlin.util.extensions.locale
import dk.nodes.nstack.kotlin.util.extensions.removeFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Locale
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
    private lateinit var koinComponent: NStackKoinComponent

    // Internally used classes
    private val classTranslationManager: ClassTranslationManager by lazy { koinComponent.classTranslationManager }
    private val viewTranslationManager: ViewTranslationManager by lazy { koinComponent.viewTranslationManager }
    private val assetCacheManager: AssetCacheManager by lazy { koinComponent.assetCacheManager }
    private val connectionManager: ConnectionManager by lazy { koinComponent.connectionManager }
    private val appInfo: ClientAppInfo by lazy { koinComponent.appInfo }
    private val networkManager: NetworkManager by lazy { koinComponent.networkManager }
    private val appOpenSettingsManager: AppOpenSettingsManager by lazy { koinComponent.appOpenSettingsManager }
    private val prefManager: PrefManager by lazy { koinComponent.prefManager }
    private val contextWrapper: ContextWrapper by lazy { koinComponent.contextWrapper }
    private val mainMenuDisplayer: MainMenuDisplayer by lazy { koinComponent.mainMenuDisplayer }
    private val termsRepository: TermsRepository by lazy { koinComponent.termsRepository }
    private val nstackMeta by lazy { koinComponent.nstackMeta }
    private val processScope by lazy { koinComponent.processScope }
    private val processLifecycle by lazy { koinComponent.processLifecycle }
    private val handleLocalizeIndexUseCase by lazy { koinComponent.handleLocalizeListUseCase }
    private val state
        get() = koinComponent.stateHolder

    // Cache Maps
    internal var networkLanguages: Map<Locale, JSONObject>? = null
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
            processScope.launch {
                val response =
                    withContext(Dispatchers.IO) { networkManager.getLocalizeResource(newLocale.toLanguageTag()) }
                if (response is Result.Success) {
                    withContext(Dispatchers.IO) { handleLocalizeIndexUseCase(response.value) }
                }
            }
            if (autoChangeLanguage) {
                language = newLocale
            }
        }
    }

    internal fun initInternal(
            context: Context,
            debugMode: Boolean,
            appId: String,
            apiKey: String,
            env: String,
            autoAppOpenEnabled: Boolean,
            vararg plugin: Any
    ) {
        NLog.i(this, "NStack initializing")
        if (isInitialized) {
            NLog.w(this, "NStack already initialized")
            return
        }
        this.debugMode = debugMode

        startKoin {
            val contextModule = module {
                single { context }
                single { createMainMenuDisplayer() }
            }
            modules(
                    managersModule,
                    nStackModule,
                    repositoryModule,
                    gsonModule,
                    httpClientModule,
                    useCaseModule,
                    contextModule
            )
        }
        koinComponent = NStackKoinComponent()

        this.appIdKey = appId
        this.appApiKey = apiKey
        this.env = env

        plugins.addAll(plugin)
        plugins += viewTranslationManager

        loadCacheTranslations()
        processLifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                registerLocaleChangeBroadcastListener(context)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                destroy(context)
            }
        })

        this.activeActivityHolder = ActiveActivityHolder()
                .also { holder -> registerActiveActivityHolderToAppLifecycle(context, holder) }

        if (Environment(env).shouldEnableTestMode) {
            versionUpdateTestMode = true
        }

        isInitialized = true

        if (autoAppOpenEnabled) {
            subscribeForAutoAppOpen()
        }
    }

    private var appOpenConsumable by consumable<Result<AppOpen>>()

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
    internal set
        /**
     * Used for settings or getting the current locale selected for language
     */
    var language: Locale = Locale.getDefault()
        internal set(value) {
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

    fun init(context: Context, debugMode: Boolean, autoAppOpenEnabled: Boolean = true, vararg plugin: Any) {
        NLog.i(this, "NStack initializing")
        if (isInitialized) {
            NLog.w(this, "NStack already initialized")
            return
        }
        this.debugMode = debugMode

        startKoin {
            val contextModule = module {
                single { context }
                single { createMainMenuDisplayer() }
            }
            modules(
                managersModule,
                nStackModule,
                repositoryModule,
                gsonModule,
                httpClientModule,
                useCaseModule,
                contextModule
            )
        }

        koinComponent = NStackKoinComponent()

        appIdKey = nstackMeta.appIdKey
        appApiKey = nstackMeta.apiKey
        env = nstackMeta.env

        plugins.addAll(plugin)
        plugins += viewTranslationManager

        loadCacheTranslations()
        processLifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                registerLocaleChangeBroadcastListener(context)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                destroy(context)
            }
        })

        this.activeActivityHolder = ActiveActivityHolder()
            .also { holder -> registerActiveActivityHolderToAppLifecycle(context, holder) }

        if (Environment(env).shouldEnableTestMode) {
            versionUpdateTestMode = true
        }

        isInitialized = true

        if (autoAppOpenEnabled) {
            subscribeForAutoAppOpen()
        }
    }

    private fun createMainMenuDisplayer(): MainMenuDisplayer {

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
     * Callback method for when the app is first opened.
     *
     * @see [AppOpen]
     */
    @Synchronized suspend fun appOpen() = state.appOpenConsumable ?: guardConnectivity {
        check(isInitialized) { "init() has not been called" }

        val localeString = language.toString()
        val appOpenSettings = appOpenSettingsManager.getAppOpenSettings()

        NLog.d(this, "onAppOpened -> $localeString $appOpenSettings")

        when (val result = networkManager.postAppOpen(appOpenSettings, localeString)) {
            is Result.Success -> {
                NLog.d(this, "NStack appOpen")

                termsRepository.setLatestTerms(result.value.data.terms)
                handleLocalizeIndexUseCase(result.value.data.localize)

                val shouldUpdateTranslationClass =
                    result.value.data.localize.any { it.shouldUpdate }
                if (shouldUpdateTranslationClass) {
                    NLog.v(this, "ShouldUpdate is set, updating Translations class...")
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
        }.also {
            state.appOpenConsumable = it
        }
    }

    /**
     * Call it to notify that the message was seen and doesn't need to appear anymore
     */
    @Deprecated(
        message = "Messages features are now accessible via NStack.Messages object",
        replaceWith = ReplaceWith(
            expression = "NStack.Messages.setMessageViewed(message)",
            imports = ["dk.nodes.nstack.kotlin.NStack"]
        )
    )
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
        NLog.v(this, "loadCacheTranslations")

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

            // Try to find the exact match
            availableLanguages
                // Do our languages match
                .find { it.language == locale.toLanguageTag().replace("-", "_").toLowerCase() }
                // Return the first value or null
                .let { languages[it] }

                ?: availableLanguages // Search our available languages for any keys that might match
                    // Do our languages match
                    .find { it.languageCode == locale.languageCode }
                    // Return the first value or null
                    .let { languages[it] }
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
        key?.cleanKeyName ?: return null
        // Try to find value in our JSON map (from cache or network)
        return  if(currentLanguage?.has(key.cleanKeyName) == true) {
            currentLanguage?.optString(key.cleanKeyName)
        // Find the value in our static Translation class as a fallback
        // (This should work as this is what the app was built with)
        } else {
            classTranslationManager.getFieldValue(key)
        }
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
     * Wrapper for [Result] returning functions that require network connectivity.
     *
     * Returns Result.Error(Error.NetworkError) when no network is available.
     */
    private suspend fun <T> guardConnectivity(block: suspend () -> Result<T>): Result<T> {
        return if (connectionManager.isConnected) {
            try {
                block()
            } catch (e: Exception) {
                Result.Error(Error.UnknownError)
            }
        } else {
            Result.Error(Error.NetworkError)
        }
    }

    /**
     * Automatically calls appOpen each time app becomes foreground.
     */
    private fun subscribeForAutoAppOpen() {
        ProcessLifecycleOwner.get().lifecycle.coroutineScope.launchWhenCreated {
            withContext(Dispatchers.IO) { appOpen() }
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

    /**
     * @see <a href="https://nstack-io.github.io/documentation/docs/features/rate-reminder.html">NStack - Rate Reminders Documentation</a>
     */
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
                title = it.title
                message = it.body
                yesButton = it.yesButton
                noButton = it.noButton
                skipButton = it.laterButton
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

    /**
     * @see <a href="https://nstack-io.github.io/documentation/docs/features/feedback.html">NStack - Feedback Documentation</a>
     */
    object Feedback {

        /**
         * Shows an activity where users can compose their feedback.
         */
        fun show(context: Context, type: FeedbackType = FeedbackType.FEEDBACK) {
            startActivity(context, Intent(context, FeedbackActivity::class.java).apply {
                putExtra(FeedbackActivity.EXTRA_FEEDBACK_TYPE, type.slug)
            }, null)
        }

        /**
         * Sends user feedback to NStack.
         *
         * This will automatically be called when you use [show]. You should only need to manually
         * use this function when writing custom views.
         */
        suspend fun postFeedback(
            name: String,
            email: String,
            message: String,
            image: ImageData?,
            type: FeedbackType
        ) = guardConnectivity {
            networkManager.postFeedback(
                settings = appOpenSettingsManager.getAppOpenSettings(),
                name = name,
                email = email,
                message = message,
                image = image?.asJpegBytes(),
                type = type
            )
        }
    }

    /**
     * @see <a href="https://nstack-io.github.io/documentation/docs/features/terms.html">NStack - Terms Documentation</a>
     */
    object Terms {

        /**
         * A list of terms which are not yet accepted by this app instance (GUID)
         *
         * This is a local copy of terms provided via [AppOpen]
         */
        val latestTerms = termsRepository.getLatestTerms().filter { it.version != null }

        /**
         * Provides latest [TermsDetails] for given [termsID]
         */
        suspend fun getTermsDetails(termsID: Long) = guardConnectivity {
            networkManager.getLatestTerms(
                termsID = termsID,
                acceptLanguage = language.toString(),
                settings = appOpenSettingsManager.getAppOpenSettings()
            )
        }

        /**
         * Sets a version of terms to viewed by this app instance (GUID)
         */
        suspend fun setTermsViewed(versionID: Long, userID: String) = guardConnectivity {
            networkManager.setTermsViewed(
                versionID = versionID,
                userID = userID,
                locale = language.toString().replace("_", "-"),
                settings = appOpenSettingsManager.getAppOpenSettings()
            )
        }
    }

    /**
     * @see <a href="https://nstack-io.github.io/documentation/docs/features/messages.html">NStack - Messages Documentation</a>
     */
    object Messages {

        /**
         * Shows an alert dialog presenting [message].
         */
        fun show(context: Context, message: Message) {
            MessageDialog(context).show(message)
        }

        /**
         * Sets a message to viewed by this app instance (GUID).
         *
         * This will automatically be called when you use [show]. You should only need to manually
         * use this function when writing custom views.
         */
        suspend fun setMessageViewed(message: Message) = guardConnectivity {
            val appOpenSettings = appOpenSettingsManager.getAppOpenSettings()
            networkManager.postMessageSeen(appOpenSettings, message.id)
        }
    }

    /**
     * @see <a href="https://nstack-io.github.io/documentation/docs/guides/Android/android-responses.html">NStack - Responses Documentation</a>
     */
    object Responses {

        /**
         * Returns the response for given [slug] as JSON String.
         */
        suspend fun getResponse(slug: String) = guardConnectivity {
            networkManager.getResponse(slug)
        }
    }

    /**
     * @see <a href="https://nstack-io.github.io/documentation/docs/features/collections.html">NStack - Collections Documentation</a>
     */
    object Collections {

        /**
         * Returns the collection for given [collectionID] as JSON String.
         */
        suspend fun getCollection(collectionID: Long) = guardConnectivity {
            networkManager.getCollection(collectionID)
        }

        /**
         * Returns the collection item for given [collectionID] and [itemID] as JSON String.
         */
        suspend fun getCollectionItem(collectionID: Long, itemID: Long) = guardConnectivity {
            networkManager.getCollectionItem(collectionID, itemID)
        }
    }
}
