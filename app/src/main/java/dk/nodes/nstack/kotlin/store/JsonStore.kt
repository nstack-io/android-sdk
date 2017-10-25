package dk.nodes.nstack.store

import kotlinx.coroutines.experimental.Deferred
import org.json.JSONObject

typealias SaveCallback = (success : Boolean) -> Unit
typealias LoadCallback = (data : JSONObject?) -> Unit
/**
 * Created by bison on 24-05-2017.
 */
interface JsonStore {
    fun save(key : String, obj : JSONObject, callback: SaveCallback = {})
    fun load(key : String) : JSONObject?
    fun loadDeferred(key : String) : Deferred<JSONObject?>
    fun loadCallback(key : String, callback : LoadCallback)
}