package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class AppUpdate(
        var state: AppUpdateState = AppUpdateState.NONE,
        var update: Update? = null
) {

    constructor(jsonObject: JSONObject) : this() {
        update = Update(
                jsonObject.optJSONObject("newer_version") ?:
                jsonObject.optJSONObject("new_in_version"))
        state = when {
            update?.state == "yes"              -> AppUpdateState.UPDATE
            update?.state == "force"            -> AppUpdateState.FORCE
            jsonObject.has("new_in_version")    -> AppUpdateState.CHANGELOG
            else                                -> AppUpdateState.NONE
        }
    }
}
