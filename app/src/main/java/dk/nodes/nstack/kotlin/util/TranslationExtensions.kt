package dk.nodes.nstack.kotlin.util

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.managers.ViewTranslationManager
import dk.nodes.nstack.kotlin.models.TranslationData
import java.lang.ref.WeakReference
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

private val lazyManager by lazy {
    val kProperty1 = NStack::class.memberProperties.find { it.name == "viewTranslationManager" }!!
    kProperty1.isAccessible = true
    kProperty1.call() as ViewTranslationManager
}

private val lazyMethod by lazy {
    ViewTranslationManager::class.declaredMemberFunctions.find { it.name == "getTranslationByKey" }!!.also {
        it.isAccessible = true
    }
}

private fun getNstackTranslationFromKey(key: String?): String? {
    return lazyMethod.call(lazyManager, cleanKeyName(key)) as? String ?: ""
}

private fun cleanKeyName(keyName: String?): String? {
    val key = keyName ?: return null
    return if (key.startsWith("{") && key.endsWith("}")) {
        key.substring(1, key.length - 1)
    } else key
}

operator fun TextView.plusAssign(key: String) {
    setTranslation(this, key)
}

operator fun ToggleButton.plusAssign(key: String) {
    setTranslation(this, key)
}


operator fun CompoundButton.plusAssign(key: String) {
    setTranslation(this, key)
}

private fun setTranslation(view: View, key: String) {
    if (getNstackTranslationFromKey(key)?.isEmpty() == true) return
    lazyManager.addView(WeakReference(view), TranslationData(key = key))
}

