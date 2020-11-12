package dk.nodes.nstack.kotlin.util

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.Date
import java.util.Locale

object GsonProvider {
    fun getGson(): Gson =
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .registerTypeAdapter(Locale::class.java, LocaleDeserializer())
            .setDateFormat(DateDeserializer.DATE_FORMAT)
            .create()
}
