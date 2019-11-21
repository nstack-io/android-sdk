package dk.nodes.nstack.kotlin.models

import com.google.gson.JsonObject

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
)

data class RateReminder2(
    val id: Int
) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getAsJsonObject("data").getAsJsonPrimitive("id").asInt
    )
}
