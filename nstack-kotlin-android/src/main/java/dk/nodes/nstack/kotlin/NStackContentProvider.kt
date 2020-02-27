package dk.nodes.nstack.kotlin

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log

class NStackContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
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
                val appId = bundle.getString("dk.nodes.nstack.appId")!!
                val apiKey = bundle.getString("dk.nodes.nstack.apiKey")!!
                val env = bundle.getString("dk.nodes.nstack.env")!!
                val translationClass = bundle.getString("dk.nodes.nstack.Translation")!!
                val isStaging = env == "staging"
                if (isStaging) {
                    NStack.baseUrl = "https://nstack-staging.vapor.cloud"
                }
                NStack.translationClass = Class.forName(translationClass)
                NStack.init(context, isStaging)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.message)
        } catch (e: NullPointerException) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.message)
        }

        return false
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

    companion object {
        private val TAG = NStackContentProvider::class.qualifiedName!!
    }
}