package dk.nodes.nstack.kotlin.util

import android.content.Context

/**
 * Use context indirectly for testing
 */
class ContextWrapper(
    private val context: Context
) {

    /**
     * Version from package info
     */
    val version: String
        get() {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                ""
            }
        }

    /**
     * List of files in assets
     */
    val assets: List<String>
        get() {
            return context.resources.assets.list("")?.asList() ?: listOf()
        }

    /**
     * Opens and reads an asset into string
     */
    fun readAsset(path: String): String {
        val inputStream = context.resources.assets.open(path)
        return inputStream.bufferedReader().use { it.readText() }
    }
}
