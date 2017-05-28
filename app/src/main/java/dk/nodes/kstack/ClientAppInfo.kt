package dk.nodes.kstack

import android.content.Context
import android.R.attr.versionName
import android.content.pm.PackageInfo



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
