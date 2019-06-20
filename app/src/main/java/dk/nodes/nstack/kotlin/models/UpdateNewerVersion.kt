package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class UpdateNewerVersion(
    val state: String,
    val lastId: Int,
    val version: String,
    val link: String,
    val translate: UpdateTranslate
) {

    constructor(jsonObject: JSONObject) : this(
        state = jsonObject.getString("state"),
        lastId = jsonObject.getInt("last_id"),
        version = jsonObject.getString("version"),
        link = jsonObject.getString("link"),
        translate = UpdateTranslate(jsonObject.getJSONObject("translate"))
    )
}
