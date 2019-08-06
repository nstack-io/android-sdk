package dk.nodes.nstack.kotlin

import android.content.Context
import dk.nodes.nstack.kotlin.managers.LiveEditManager

fun NStack.enableLiveEdit(context: Context) {
    LiveEditManager(context, language.toString().replace("_", "-"))
}
