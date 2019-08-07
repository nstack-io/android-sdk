package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class Message(
        val id: Int,
        val applicationId: Int,
        val platform: String,
        val showSetting: String,
        val viewCount: Int,
        val message: String
) {


    constructor(jsonObject: JSONObject): this(
            id = jsonObject.getInt("id"),
            applicationId = jsonObject.getInt("application_id"),
            platform = jsonObject.getString("platform"),
            showSetting = jsonObject.getString("show_setting"),
            viewCount = jsonObject.getInt("view_count"),
            message = jsonObject.getString("message")
    )

}



