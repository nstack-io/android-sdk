package dk.nodes.nstack.models

import android.content.Context

class ClientAppInfo(context: Context) {
    val packageName: String = context.packageName
    val versionName: String
    val versionCode: Int

    init {
        val pInfo = context.packageManager.getPackageInfo(packageName, 0)
        versionName = pInfo.versionName
        versionCode = pInfo.versionCode
    }
}
