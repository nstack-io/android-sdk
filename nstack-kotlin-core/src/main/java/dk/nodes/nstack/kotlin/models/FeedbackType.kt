package dk.nodes.nstack.kotlin.models

enum class FeedbackType(val slug: String) {
    FEEDBACK(slug = "feedback"),
    BUG(slug = "bug");

    companion object {
        fun fromSlug(slug: String?): FeedbackType = values().find { it.slug == slug } ?: FEEDBACK
    }
}