package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class Message(
        val id: Int,
        val showSetting: String,
        val message: String
) {


    constructor(jsonObject: JSONObject): this(
            id = jsonObject.getInt("id"),
            showSetting = jsonObject.getString("show_setting"),
            message = jsonObject.getString("message")
    )

}



