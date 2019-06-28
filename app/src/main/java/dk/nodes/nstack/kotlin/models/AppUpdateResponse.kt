package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class AppUpdateResponse(
    val data: AppUpdateData,
    val meta: AppUpdateMeta
) {

    constructor(jsonObject: JSONObject) : this(
        data = AppUpdateData(jsonObject.getJSONObject("data")),
        meta = AppUpdateMeta(jsonObject.getJSONObject("meta"))
    )
}
