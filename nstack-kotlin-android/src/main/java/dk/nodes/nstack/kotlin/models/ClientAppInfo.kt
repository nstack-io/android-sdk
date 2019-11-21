package dk.nodes.nstack.kotlin.models

import android.content.Context
import android.os.Build

class ClientAppInfo(context: Context) {
    val packageName: String = context.packageName
    val versionName: String
    val versionCode: Int
    val deviceName: String
    val osVersion: String

    init {
        val pInfo = context.packageManager.getPackageInfo(packageName, 0)
        versionName = pInfo.versionName
        versionCode = pInfo.versionCode
        deviceName = Build.MODEL
        osVersion = Build.VERSION.RELEASE
    }
}
