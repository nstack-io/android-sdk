package dk.nodes.nstack.kotlin.models

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import dk.nodes.nstack.kotlin.util.NLog

internal class NStackMeta(context: Context) {

    val appIdKey: String
    val apiKey: String
    val env: String

    init {
        val applicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )

        val meta = applicationInfo.metaData

        appIdKey = meta.getMetaString(APPID_KEY)
        apiKey = meta.getMetaString(API_KEY)
        env = meta.getMetaString(ENV_KEY)
    }

    private fun Bundle.getMetaString(key: String): String {
        return if (containsKey(key)) {
            getString(key) ?: ""
        } else {
            NLog.e(this, "Missing $key")
            ""
        }
    }

    companion object {

        private const val APPID_KEY = "dk.nodes.nstack.appId"
        private const val API_KEY = "dk.nodes.nstack.apiKey"
        private const val ENV_KEY = "dk.nodes.nstack.env"
    }
}
