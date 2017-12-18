package dk.nodes.nstack.kotlin.util

import java.util.*

class LocaleUtils {
    companion object {
        fun getLocalFromString(localeString: String?): Locale {
            if (localeString == null) {
                return Locale.getDefault()
            }

            val splitLocale = when {
                localeString.contains("_") -> localeString.split("_")
                localeString.contains("-") -> localeString.split("-")
                else -> arrayListOf("en", "gb")
            }

            val language = splitLocale[0]
            val country = splitLocale[1]

            return Locale(language, country)
        }
    }
}