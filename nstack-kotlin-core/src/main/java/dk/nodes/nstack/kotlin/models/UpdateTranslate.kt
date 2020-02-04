package dk.nodes.nstack.kotlin.models
import com.google.gson.annotations.SerializedName

data class UpdateTranslate(
        val title: String,
        val message: String,
        @SerializedName("positiveBtn") val positiveButton: String,
        @SerializedName("negativeBtn") val negativeButton: String
)
