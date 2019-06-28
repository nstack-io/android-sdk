package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class AppUpdateMeta(
    val acceptLanguage: String?
) {

    constructor(jsonObject: JSONObject) : this(
        acceptLanguage = jsonObject.optString("accept_Language")
    )
}
