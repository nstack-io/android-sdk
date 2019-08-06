package dk.nodes.nstack.kotlin.plugin

import android.view.View
import dk.nodes.nstack.kotlin.models.TranslationData
import java.lang.ref.WeakReference

interface NStackViewPlugin: NStackPlugin {
    fun addView(weakView: WeakReference<View>, translationData: TranslationData)
}