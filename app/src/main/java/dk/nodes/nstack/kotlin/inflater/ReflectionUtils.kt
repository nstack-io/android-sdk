package dk.nodes.nstack.kotlin.inflater

import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectionUtils {

    fun getField(clazz: Class<*>, fieldName: String): Field? {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            return field
        } catch (ignored: NoSuchFieldException) {
        }
        return null
    }

    fun getValue(field: Field, obj: Any): Any? {
        try {
            return field.get(obj)
        } catch (ignored: IllegalAccessException) {
        }
        return null
    }

    fun setValue(field: Field, obj: Any, value: Any) {
        try {
            field.set(obj, value)
        } catch (ignored: IllegalAccessException) {
        }
    }

    fun getMethod(clazz: Class<*>, methodName: String): Method? {
        val methods = clazz.methods
        for (method in methods) {
            if (method.name == methodName) {
                method.isAccessible = true
                return method
            }
        }
        return null
    }

    fun invokeMethod(obj: Any, method: Method?, vararg args: Any) {
        try {
            if (method == null) {
                return
            }
            method.invoke(obj, args)
        } catch (e: Exception) {
        }
    }
}
