package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

/**
 * Info for rate reminder dialog
 */
data class RateReminder(
    val title: String,
    val body: String,
    val yesButton: String,
    val laterButton: String,
    val noButton: String,
    val link: String
) {

    constructor(jsonObject: JSONObject) : this(
        title = jsonObject.getString("title"),
        body = jsonObject.getString("body"),
        yesButton = jsonObject.getString("yesBtn"),
        noButton = jsonObject.getString("noBtn"),
        laterButton = jsonObject.getString("laterBtn"),
        link = jsonObject.getString("link")
    )
}
