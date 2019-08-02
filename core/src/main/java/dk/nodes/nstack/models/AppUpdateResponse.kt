package dk.nodes.nstack.models

import org.json.JSONObject

data class AppUpdateResponse(
    val data: AppUpdateData,
    val meta: AppUpdateMeta
) {

    constructor(jsonObject: JSONObject) : this(
        data = jsonObject.getJSONObject("data").appUpdateData,
        meta = AppUpdateMeta(jsonObject.getJSONObject("meta"))
    )
}
