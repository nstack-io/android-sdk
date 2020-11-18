package dk.nodes.nstack.kotlin

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import dk.nodes.nstack.kotlin.util.NLog

class NStackContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        Log.d(TAG, "OnCreate")
        try {
            val context = context ?: return false
            val ai: ApplicationInfo = context.packageManager
                    .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle: Bundle = ai.metaData
            if (
                    bundle.containsKey("dk.nodes.nstack.appId") &&
                    bundle.containsKey("dk.nodes.nstack.apiKey") &&
                    bundle.containsKey("dk.nodes.nstack.env") &&
                    bundle.containsKey("dk.nodes.nstack.Translation")
            ) {
                val appId = bundle.getString("dk.nodes.nstack.appId")!!.also {
                    log("AppId $it")
                }
                val apiKey = bundle.getString("dk.nodes.nstack.apiKey")!!.also {
                    log("apiKey $it")
                }
                val env = bundle.getString("dk.nodes.nstack.env")!!.also {
                    log("env $it")
                }
                val translationClass = bundle.getString("dk.nodes.nstack.Translation")!!.also {
                    log("translationClass $it")
                }
                NStack.translationClass = Class.forName(translationClass)
                NStack.initInternal(
                        context,
                        context.getBuildConfigField("DEBUG", false),
                        appId,
                        apiKey,
                        env
                )
                log("Init success")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.message)
        } catch (e: NullPointerException) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.message)
        }

        return false
    }

    private fun log(string: String) {
        Log.d(TAG, string)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
    ): Cursor? = null

    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?
    ) = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0

    override fun getType(uri: Uri): String? = null

    private inline fun <reified T> Context.getBuildConfigField(fieldName: String, defaultValue: T): T {
        try {
            val clazz = Class.forName(javaClass.getPackage()!!.name + ".BuildConfig")
            val field = clazz.getField(fieldName)
            return (field.get(null) as? T) ?: defaultValue
        } catch (e: ClassNotFoundException) {
            NLog.d(TAG, "Unable to get the BuildConfig, is this built with ANT?")
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            NLog.d(TAG, "$fieldName is not a valid field. Check your build.gradle")
        } catch (e: IllegalAccessException) {
            NLog.d(TAG, "Illegal Access Exception: Let's print a stack trace.")
            e.printStackTrace()
        } catch (e: NullPointerException) {
            NLog.d(TAG, "Null Pointer Exception: Let's print a stack trace.")
            e.printStackTrace()
        }
        return defaultValue
    }

    companion object {
        private val TAG = NStackContentProvider::class.qualifiedName!!
    }
}