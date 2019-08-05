package dk.nodes.nstack.kotlin.inflater

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.models.TranslationData
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.extensions.children

class NStackRootLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // TODO: Look into performance of this vs current solution

        if (changed) {
            children.forEach {
                processView(it)
            }
        }
    }

    override fun onInterceptTouchEvent(motionEvent: MotionEvent): Boolean {
        val view = findViewAtPosition(this, motionEvent.x.toInt(), motionEvent.y.toInt())

        Log.d("NStackRootLayout", "view: ${view?.javaClass?.simpleName}")

        // TODO: Check if view is supported by NStack
        // TODO: Check if view has text from NStack
        // TODO: Show 'LiveEditDialog' if all above is valid

        return super.onInterceptTouchEvent(motionEvent)
    }

    private fun findViewAtPosition(parent: View, x: Int, y: Int): View? {
        val rect = Rect()
        val viewsBelowPosition = ArrayList<View>()

        if (parent is ViewGroup) {
            parent.children.forEach {
                it.getGlobalVisibleRect(rect)
                if (rect.contains(x, y)) {
                    viewsBelowPosition.add(it)
                }
            }
        }

        viewsBelowPosition.reverse()
        return viewsBelowPosition.firstOrNull()
    }

    /**
     * Takes values of a NStack supported View and adds them to the NStack Translation Library Cache
     */
    private fun processView(view: View) {
        val translationData = TranslationData()

        if (view is TextView) {
            translationData.key = view.text?.toString()?.obtainNStackKeyName()
        }

        // TODO: Add support for other Views (Button, Toolbar, ...)

        if (translationData.isValid()) {
            NStack.addView(view, translationData)
        } else {
            NLog.d(this, "processView -> Found no valid NStack keys ${view.javaClass.name}")
        }
    }

    private fun TranslationData.isValid(): Boolean {
        return !(key == null &&
                text == null &&
                hint == null &&
                description == null &&
                textOn == null &&
                textOff == null &&
                contentDescription == null &&
                title == null &&
                subtitle == null)
    }

    private fun String.obtainNStackKeyName(): String? {
        return when {
            startsWith("{") && endsWith("}") -> substring(1, length - 1)
            else -> null
        }
    }
}