package dk.nodes.nstack.kotlin.provider

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dk.nodes.nstack.kotlin.util.DateDeserializer
import dk.nodes.nstack.kotlin.util.LocaleDeserializer
import org.koin.dsl.module
import java.util.Date
import java.util.Locale

val gsonModule = module {
    single {
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .registerTypeAdapter(Locale::class.java, LocaleDeserializer())
            .setDateFormat(DateDeserializer.DATE_FORMAT)
            .create()
    }
}