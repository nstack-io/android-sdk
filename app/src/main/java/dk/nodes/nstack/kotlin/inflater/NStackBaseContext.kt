package dk.nodes.nstack.kotlin.inflater

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater

class NStackBaseContext(context: Context) : ContextWrapper(context) {
    override fun getSystemService(name: String?): Any {

        if (Context.LAYOUT_INFLATER_SERVICE.equals(name, true)) {
            val layoutInflater = LayoutInflater.from(baseContext)
            return NStackLayoutInflater(layoutInflater, baseContext, false)
        }

        return super.getSystemService(name)
    }
}