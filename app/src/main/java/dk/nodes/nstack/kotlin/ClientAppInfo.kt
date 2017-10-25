package dk.nodes.nstack.kotlin

import android.content.Context


/**
 * Created by bison on 28-05-2017.
 */
class ClientAppInfo(context : Context) {
    val packageName : String = context.packageName
    val versionName : String
    val versionCode : Int

    init {
        val pInfo = context.packageManager.getPackageInfo(packageName, 0)
        versionName = pInfo.versionName
        versionCode = pInfo.versionCode
    }
}
