package dk.nodes.nstack.kotlin.inflater

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import dk.nodes.nstack.kotlin.util.getChildrenViews

class NStackRootLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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
            parent.getChildrenViews().forEach {
                it.getGlobalVisibleRect(rect)
                if (rect.contains(x, y)) {
                    viewsBelowPosition.add(it)
                }
            }
        }

        viewsBelowPosition.reverse()
        return viewsBelowPosition.firstOrNull()
    }
}