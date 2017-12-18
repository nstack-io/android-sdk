package dk.nodes.nstack.kotlin

import android.support.v7.app.AlertDialog
import dk.nodes.nstack.kotlin.models.UpdateType
import java.text.SimpleDateFormat
import java.util.*
// extension
fun Date.parseFromISO8601(str : String)
{
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    try {
        this.time = format.parse(str).time
    }
    catch (e : Exception) {
    }
}

// log function defintion, yay typedef is back :D
typealias LogFunction = (tag: String, msg: String) -> Unit
// data/model class for storing information about available languages
typealias VersionControlCallback = (type: UpdateType, builder: AlertDialog.Builder?) -> Unit
typealias AppOpenCallback = (success: Boolean) -> Unit

internal var nLog: LogFunction = fun(tag, msg) {
    println("$tag : $msg")
}