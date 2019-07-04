package dk.nodes.nstack.kotlin.models

import dk.nodes.nstack.kotlin.util.iso8601Date
import org.json.JSONObject
import java.util.*

data class LocalizeIndex(
    val id: Int,
    val url: String,
    val lastUpdatedAt: Date,
    val shouldUpdate: Boolean,
    val language: Language
) {

    constructor(jsonObject: JSONObject) : this(
        id = jsonObject.getInt("id"),
        url = jsonObject.getString("url"),
        lastUpdatedAt = jsonObject.getString("last_updated_at").iso8601Date,
        shouldUpdate = jsonObject.getBoolean("should_update"),
        language = Language(jsonObject.getJSONObject("language"))
    )
}

