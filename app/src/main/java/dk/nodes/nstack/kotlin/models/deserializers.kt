@file:Suppress("FunctionName")

package dk.nodes.nstack.kotlin.models

import dk.nodes.nstack.kotlin.util.extensions.iso8601Date
import dk.nodes.nstack.kotlin.util.extensions.localizeIndices
import dk.nodes.nstack.models.AppUpdate
import dk.nodes.nstack.models.AppUpdateData
import dk.nodes.nstack.models.AppUpdateMeta
import dk.nodes.nstack.models.AppUpdateResponse
import dk.nodes.nstack.models.AppUpdateState
import dk.nodes.nstack.models.Language
import dk.nodes.nstack.models.LocalizeIndex
import dk.nodes.nstack.models.Message
import dk.nodes.nstack.models.RateReminder
import dk.nodes.nstack.models.Update
import dk.nodes.nstack.models.UpdateTranslate
import org.json.JSONObject
import java.util.Locale

fun AppUpdate(jsonObject: JSONObject): AppUpdate {
    val update = Update(
        jsonObject.optJSONObject("newer_version") ?: jsonObject.optJSONObject("new_in_version")
    )
    return AppUpdate(

        update = update,
        state = when {
            update.state == "yes" -> AppUpdateState.UPDATE
            update.state == "force" -> AppUpdateState.FORCE
            jsonObject.has("new_in_version") -> AppUpdateState.CHANGELOG
            else -> AppUpdateState.NONE
        }
    )
}

val JSONObject.appUpdateData: AppUpdateData
    get() {
        val count = optInt("count")
        val localize = getJSONArray("localize").localizeIndices
        val platform = getString("platform")
        val createdAt = getString("created_at").iso8601Date
        val lastUpdated = getString("last_updated").iso8601Date

        val update = if (has("update")) {
            AppUpdate(getJSONObject("update"))
        } else {
            AppUpdate()
        }

        val message = if (has("message")) {
            Message(getJSONObject("message"))
        } else {
            null
        }

        val rateReminder = if (has("rate_reminder")) {
            RateReminder(getJSONObject("rate_reminder"))
        } else {
            null
        }

        return AppUpdateData(
            count = count,
            localize = localize,
            platform = platform,
            createdAt = createdAt,
            lastUpdated = lastUpdated,
            update = update,
            message = message,
            rateReminder = rateReminder
        )
    }

fun AppUpdateMeta(jsonObject: JSONObject) =
    AppUpdateMeta(acceptLanguage = jsonObject.optString("accept_Language"))

fun AppUpdateResponse(jsonObject: JSONObject) =
    AppUpdateResponse(
        data = jsonObject.getJSONObject("data").appUpdateData,
        meta = AppUpdateMeta(jsonObject.getJSONObject("meta"))
    )

fun Language(jsonObject: JSONObject) = Language(
    id = jsonObject.getInt("id"),
    name = jsonObject.getString("name"),
    locale = Locale(jsonObject.getString("locale")),
    direction = jsonObject.getString("direction"),
    isDefault = jsonObject.getBoolean("is_default"),
    isBestFit = jsonObject.getBoolean("is_best_fit")
)

fun LocalizeIndex(jsonObject: JSONObject) = LocalizeIndex(
    id = jsonObject.getInt("id"),
    url = jsonObject.getString("url"),
    lastUpdatedAt = jsonObject.getString("last_updated_at").iso8601Date,
    shouldUpdate = jsonObject.getBoolean("should_update"),
    language = Language(jsonObject.getJSONObject("language"))
)

fun Message(jsonObject: JSONObject) = Message(
    id = jsonObject.getInt("id"),
    projectId = jsonObject.getInt("project_id"),
    platform = jsonObject.getString("platform"),
    showSetting = jsonObject.getString("show_setting"),
    viewCount = jsonObject.getInt("view_count"),
    message = jsonObject.getString("message")
)

fun RateReminder(jsonObject: JSONObject) = RateReminder(
    title = jsonObject.getString("title"),
    body = jsonObject.getString("body"),
    yesButton = jsonObject.getString("yesBtn"),
    noButton = jsonObject.getString("noBtn"),
    laterButton = jsonObject.getString("laterBtn"),
    link = jsonObject.getString("link")
)

fun Update(jsonObject: JSONObject) = Update(
    state = jsonObject.optString("state", "none"),
    lastId = jsonObject.getInt("last_id"),
    version = jsonObject.getString("version"),
    link = jsonObject.optString("link", "empty"),
    translate = UpdateTranslate(jsonObject.getJSONObject("translate")),
    fileUrl = jsonObject.optString("file_url")
)

fun UpdateTranslate(jsonObject: JSONObject) = UpdateTranslate(
    title = jsonObject.optString("title"),
    message = jsonObject.optString("message"),
    positiveButton = jsonObject.optString("positiveBtn"),
    negativeButton = jsonObject.optString("negativeBtn")
)