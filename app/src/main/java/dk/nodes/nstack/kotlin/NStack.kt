package dk.nodes.nstack.kotlin

import android.content.Context
import android.content.pm.PackageManager
import com.google.gson.JsonObject
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.store.AssetCacheManager
import dk.nodes.nstack.kotlin.util.NLog

object NStack {
    private val TAG = this.javaClass.simpleName
    // Has our app been started yet?
    private var isInitialized: Boolean = false

    // Variables
    var appIdKey: String = ""
    var appApiKey: String = ""

    // Client Information
    lateinit var assetCacheManager: AssetCacheManager
    lateinit var clientAppInfo: ClientAppInfo

    // Cache Maps
    var assetLanguageCache: HashMap<String, JsonObject> = hashMapOf()

    // States
    var debugMode: Boolean = false
        set(value) {
            field = value
            onDebugModeChanged()
        }

    fun init(context: Context) {
        NLog.i(TAG, "NStack initializing")

        if (isInitialized) {
            NLog.w(TAG, "NStack already initialized")
            return
        }

        getApplicationInfo(context)

        assetCacheManager = AssetCacheManager(context)
        clientAppInfo = ClientAppInfo(context)

        loadAssetCache()

        NLog.d(TAG, "DEBUG :" + assetLanguageCache.toString())
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

    private fun loadAssetCache() {
        assetCacheManager.loadFromAssetCache()
    }

    /**
     * On State Change Listeners
     */

    private fun onDebugModeChanged() {
        NLog.enableLogging = debugMode
    }
}