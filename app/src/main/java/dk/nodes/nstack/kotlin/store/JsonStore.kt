package dk.nodes.nstack.kotlin.store

import dk.nodes.nstack.kotlin.util.LoadCallback
import dk.nodes.nstack.kotlin.util.SaveCallback
import kotlinx.coroutines.experimental.Deferred
import org.json.JSONObject


interface JsonStore {
    fun save(key: String, obj: JSONObject, callback: SaveCallback = {})
    fun load(key: String): JSONObject?
    fun loadDeferred(key: String): Deferred<JSONObject?>
    fun loadCallback(key: String, callback: LoadCallback)
}