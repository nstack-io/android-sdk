package dk.nodes.nstack.kotlin.models

import com.google.gson.annotations.SerializedName

data class UpdateTranslate(
    @SerializedName("title", alternate = ["updateHeader", "forceHeader", "newInVersionHeader"])
    val title: String,
    val message: String,
    @SerializedName("positiveBtn", alternate = ["positiveButton"])
    val positiveButton: String,
    @SerializedName("negativeBtn", alternate = ["negativeButton"])
    val negativeButton: String
)
