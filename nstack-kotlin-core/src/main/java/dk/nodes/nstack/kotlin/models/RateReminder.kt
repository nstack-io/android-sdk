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
    val id: Int,
    val title: String,
    val body: String,
    val yesButton: String,
    val laterButton: String,
    val noButton: String
) {

    companion object {
        fun parse(jsonObject: JsonObject): RateReminder2? {
            return try {
                val data = jsonObject.getAsJsonObject("data")
                val localization = data.getAsJsonObject("localization")
                RateReminder2(
                        id = data.getAsJsonPrimitive("id").asInt,
                        title = localization.getAsJsonPrimitive("title").asString,
                        body = localization.getAsJsonPrimitive("body").asString,
                        yesButton = localization.getAsJsonPrimitive("yesBtn").asString,
                        laterButton = localization.getAsJsonPrimitive("laterBtn").asString,
                        noButton = localization.getAsJsonPrimitive("noBtn").asString
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
