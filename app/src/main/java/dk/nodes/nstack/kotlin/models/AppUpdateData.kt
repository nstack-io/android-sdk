package dk.nodes.nstack.kotlin.models

import dk.nodes.nstack.kotlin.util.iso8601Date
import dk.nodes.nstack.kotlin.util.localizeIndices
import org.json.JSONObject
import java.util.*

data class AppUpdateData(
    val count: Int = 0,
    val update: AppUpdate = AppUpdate(),
    val localize: List<LocalizeIndex> = listOf(),
    val platform: String = "",
    val createdAt: Date = Date(),
    val lastUpdated: Date = Date()
) {

    constructor(jsonObject: JSONObject) : this(
        count = jsonObject.optInt("count"),
        update = AppUpdate(jsonObject.getJSONObject("update")),
        localize = jsonObject.getJSONArray("localize").localizeIndices,
        platform = jsonObject.getString("platform"),
        createdAt = jsonObject.getString("created_at").iso8601Date,
        lastUpdated = jsonObject.getString("last_updated").iso8601Date
    )
}
