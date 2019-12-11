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

//  {"data":{"id":13,"points_to_trigger":2,"days_delay_on_skip":0,"localization":{"title":"Rate the app","body":"We can see you like the application. Would you like to rate it?","yesBtn":"Yes","laterBtn":"Later","noBtn":"No"}}}
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
                RateReminder2(
                        id = jsonObject.getAsJsonObject("data").getAsJsonPrimitive("id").asInt,
                        title = jsonObject.getAsJsonObject("data").getAsJsonObject("localization").getAsJsonObject("title").asString,
                        body = jsonObject.getAsJsonObject("data").getAsJsonObject("localization").getAsJsonObject("body").asString,
                        yesButton = jsonObject.getAsJsonObject("data").getAsJsonObject("localization").getAsJsonObject("yesBtn").asString,
                        laterButton = jsonObject.getAsJsonObject("data").getAsJsonObject("localization").getAsJsonObject("laterBtn").asString,
                        noButton = jsonObject.getAsJsonObject("data").getAsJsonObject("localization").getAsJsonObject("noBtn").asString
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
