package dk.nodes.nstack.kotlin.inflater

import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectionUtils {

    fun getField(clazz: Class<*>, fieldName: String): Field? {
        return try {
            clazz.getDeclaredField(fieldName).also { it.isAccessible = true }
        } catch (e: NoSuchFileException) {
            null
        }
    }

    fun getValue(field: Field, obj: Any): Any? {
        return try {
            field.get(obj)
        } catch (e: IllegalAccessException) {
            null
        }
    }

    fun setValue(field: Field, obj: Any, value: Any) {
        try {
            field.set(obj, value)
        } catch (ignored: IllegalAccessException) {
        }
    }

    fun getMethod(clazz: Class<*>, methodName: String): Method? {
        return clazz.methods.firstOrNull { it.name == methodName }?.also { it.isAccessible = true }
    }

    fun invokeMethod(obj: Any, method: Method?, vararg args: Any) {
        try {
            method?.invoke(obj, args)
        } catch(e: Exception) {
        }
    }
}
