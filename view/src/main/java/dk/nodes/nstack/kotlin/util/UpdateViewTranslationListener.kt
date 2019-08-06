package dk.nodes.nstack.kotlin.util

import android.view.View
import dk.nodes.nstack.kotlin.models.TranslationData

typealias UpdateViewTranslationListener = (view: View, translationPair: Pair<TranslationData, TranslationData>) -> Unit