package dk.nodes.nstack.kotlin.inflater

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.view.LayoutInflater
import dk.nodes.nstack.kotlin.NStack

class NStackBaseContext(context: Context) : ContextWrapper(context) {

    /**
     * signature from Context.java:
     * public abstract @Nullable Object getSystemService(@ServiceName @NonNull String name);
     */
    override fun getSystemService(name: String): Any? {
        // apparently sometimes this can be called with null on certain samsung phones
        if (name == null) {
            return null
        }

        if (Context.LAYOUT_INFLATER_SERVICE.equals(name, true)) {
            return NStackLayoutInflater(LayoutInflater.from(baseContext), baseContext, false)
        }

        return super.getSystemService(name)
    }

    private val resources = object : Resources(
        context.assets,
        context.resources.displayMetrics,
        context.resources.configuration
    ) {
        override fun getString(id: Int): String {
            return NStack.getTranslation(id, context) ?: super.getString(id)
        }
    }

    override fun getResources(): Resources {
        return resources
    }
}
