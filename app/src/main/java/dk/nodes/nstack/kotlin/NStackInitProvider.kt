package dk.nodes.nstack.kotlin

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * Created by Christian on 7/6/2017.
 * Takes advantage of the fact that a ContentProvider has access to the context of the client app
 * Looks-up the appId and apiKey from the manifest meta-data, so in your app's build-gradle, add
 * the placeholders there in the defaultconfig; for example:
 * manifestPlaceholders = [
 *      appId:"IFeoifIMFIUFN8FfynFNLKJFiKLIFhUIFfnF",
 *      apiKey:"oingiuyNyfng7y8NUYbnfgutyguMUim09hb"]
 * @author   Bison
 * @since    05/01/17.
 */
class NStackInitProvider : ContentProvider() {
    val TAG = javaClass.kotlin.simpleName

    override fun onCreate(): Boolean {
//        Log.i(TAG, "NStack init provider onCreate")
        try {
            val ai = context!!.packageManager.getApplicationInfo(context!!.packageName, PackageManager.GET_META_DATA)
            val bundle = ai.metaData
            if (bundle.containsKey("dk.nodes.nstack.appId") && bundle.containsKey("dk.nodes.nstack.apiKey")) {
                val appId = bundle.getString("dk.nodes.nstack.appId")
                val apiKey = bundle.getString("dk.nodes.nstack.apiKey")
                Log.d(TAG, "Read appId = $appId apiKey = $apiKey")
                NStack.init(context, appId, apiKey)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.message)
        } catch (e: NullPointerException) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.message)
        }

        return false
    }

    override fun query(uri: Uri, strings: Array<String>?, s: String?, strings1: Array<String>?, s1: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, contentValues: ContentValues?, s: String?, strings: Array<String>?): Int {
        return 0
    }
}