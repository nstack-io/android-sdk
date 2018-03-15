package dk.nodes.nstack.kotlin.util

import dk.nodes.nstack.kotlin.models.AppUpdate
import dk.nodes.nstack.kotlin.models.AppUpdateState
import org.json.JSONObject

fun AppUpdate.parseFromString(string: String) {
    val jsonRoot: JSONObject? = JSONObject(string)
    val jsonData: JSONObject? = jsonRoot?.optJSONObject("data")
    val jsonUpdate: JSONObject? = jsonData?.optJSONObject("update")
    val jsonVersion: JSONObject? = jsonUpdate?.optJSONObject("newer_version")

    versionName = jsonVersion?.optString("version", null)
    versionCode = jsonVersion?.optInt("last_id", 0)
    link = jsonVersion?.optString("link", null)

    val stateValue = jsonVersion?.optString("state", null)

    state = when (stateValue) {
        "yes"       -> AppUpdateState.UPDATE
        "force"     -> AppUpdateState.FORCE
        "changelog" -> AppUpdateState.CHANGELOG
        else        -> AppUpdateState.NONE
    }

    val translate: JSONObject? = jsonVersion?.optJSONObject("translate")

    if (translate != null) {
        title = translate.optString("title", null)
        message = translate.optString("message", null)
        positiveBtn = translate.optString("positiveBtn", null)
        negativeBtn = translate.optString("negativeBtn", null)
    }
}