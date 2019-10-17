package dk.nodes.nstack.kotlin.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Terms(
        val id: Long,
        val type: Type,
        val name: String,
        val slug: String,
        val version: Version?
) {
    data class Version(
            val id: Long,
            val version: String,
            @SerializedName("version_name")
            val name: String,
            @SerializedName("published_at")
            val publishedAt: Date,
            @SerializedName("has_viewed")
            val hasViewed: Boolean
    )

    enum class Type {
        @SerializedName("terms-conditions")
        TERMS_CONDITIONS,
        @SerializedName("privacy")
        PRIVACY,
        @SerializedName("cookie")
        COOKIE,
        @SerializedName("other")
        OTHER
    }
}