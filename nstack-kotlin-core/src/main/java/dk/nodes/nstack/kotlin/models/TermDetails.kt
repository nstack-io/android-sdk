package dk.nodes.nstack.kotlin.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class TermDetails(
        @SerializedName("id")
        val versionID: Long,
        val version: Long,
        @SerializedName("version_name")
        val versionName: String,
        @SerializedName("published_at")
        val publishedAt: Date,
        @SerializedName("has_viewed")
        val hasViewed: Boolean,
        val content: Content
) {
    data class Content(
            val data: String,
            val locale: String
    )
}