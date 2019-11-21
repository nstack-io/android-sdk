package dk.nodes.nstack.kotlin.provider

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dk.nodes.nstack.kotlin.util.DateDeserializer
import dk.nodes.nstack.kotlin.util.LocaleDeserializer
import java.util.Date
import java.util.Locale
import kotlin.reflect.KClass

object GsonProvider {

    fun provideGson(): Gson {
        return getLazyDependency(Gson::class) {
            GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date::class.java, DateDeserializer())
                .registerTypeAdapter(Locale::class.java, LocaleDeserializer())
                .setDateFormat(DateDeserializer.DATE_FORMAT)
                .create()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <T : Any> getLazyDependency(
        clazz: KClass<T>,
        crossinline block: () -> T
    ): T {
        if (!dependenciesMap.containsKey(clazz)) {
            dependenciesMap[clazz] = block()
        }
        return dependenciesMap[clazz] as T
    }

    private val dependenciesMap = mutableMapOf<KClass<*>, Any>()
}
