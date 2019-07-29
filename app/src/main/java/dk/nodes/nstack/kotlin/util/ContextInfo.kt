package dk.nodes.nstack.kotlin.util

import android.content.Context

/**
 * Use context indirectly for testing
 */
class ContextInfo(
    private val context: Context
) {

    val version: String
        get() {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                ""
            }
        }
}
