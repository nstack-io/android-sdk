package dk.nodes.nstack.kotlin.inflater

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater


class NStackBaseContext(context: Context) : ContextWrapper(context) {

    /**
     * signature from Context.java:
     * public abstract @Nullable Object getSystemService(@ServiceName @NonNull String name);
     */
    override fun getSystemService(name: String): Any? {
        // apparently sometimes this can be called with null on certain samsung phones

        if (Context.LAYOUT_INFLATER_SERVICE.equals(name, true)) {
            return NStackLayoutInflater(LayoutInflater.from(baseContext), baseContext, false)
        }

        return super.getSystemService(name)
    }
}
