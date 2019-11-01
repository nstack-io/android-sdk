package dk.nodes.nstack.kotlin.models

import com.google.gson.annotations.SerializedName

data class Message(
    val id: Int,
    val showSetting: ShowSetting,
    val message: String,
    val url: String?,
    val localization: Localization
) {
    data class Localization(
        val okBtn: String?,
        val urlBtn: String?
    )
    enum class ShowSetting {
        @SerializedName("show_once")
        ONCE,
        @SerializedName("show_always")
        ALWAYS
    }
}
