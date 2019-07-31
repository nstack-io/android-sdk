package dk.nodes.nstack.kotlin.util

import android.content.Context
import android.os.Handler


internal class ContextWrapper(
    private val context: Context
) {

    private var handler: Handler = Handler()

    val assets: List<String>
        get() {
            return context.resources.assets.list("")?.asList() ?: listOf()
        }

    fun readAsset(path: String): String {
        val inputStream = context.resources.assets.open(path)
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun runUiAction(action: () -> Unit) {
        handler.post {
            action()
        }
    }
}
