package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class UpdateTranslate(
    val title: String,
    val message: String,
    val positiveButton: String,
    val negativeButton: String
) {

    constructor(jsonObject: JSONObject) : this(
        title = jsonObject.optString("title"),
        message = jsonObject.optString("message"),
        positiveButton = jsonObject.optString("positiveBtn"),
        negativeButton = jsonObject.optString("negativeBtn")
    )
}
