package io.github.kitoku95.mediaarchive.domain.model

/**
 * Supported application display languages.
 */
enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    VIETNAMESE("vi"),
    JAPANESE("ja");

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return ENGLISH
            return entries.firstOrNull {
                tag.equals(it.tag, ignoreCase = true) ||
                    tag.startsWith("${it.tag}-", ignoreCase = true)
            } ?: ENGLISH
        }
    }
}
