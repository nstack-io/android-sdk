package dk.nodes.nstack.kotlin.util

import dk.nodes.nstack.kotlin.appopen.AppUpdate
import org.json.JSONObject

fun AppUpdate.parseFromString(string: String) {
    val jsonRoot: JSONObject? = JSONObject(string)
    val jsonData: JSONObject? = jsonRoot?.optJSONObject("data")
    val jsonUpdate: JSONObject? = jsonData?.optJSONObject("update")
    val jsonVersion: JSONObject? = jsonUpdate?.optJSONObject("newer_version")

    state = jsonVersion?.optString("state", null)
    versionName = jsonVersion?.optString("version", null)
    versionCode = jsonVersion?.optInt("last_id", 0)
    link = jsonVersion?.optString("link", null)
    state = jsonVersion?.optString("state", null)

    if (state == "force") {
        force = true
        isUpdate = true
    }

    if (state == "yes") {
        isUpdate = true
    }

    val translate: JSONObject? = jsonVersion?.optJSONObject("translate")

    if (translate != null) {
        title = translate.optString("title", null)
        message = translate.optString("message", null)
        positiveBtn = translate.optString("positiveBtn", null)
        negativeBtn = translate.optString("negativeBtn", null)
    }
}