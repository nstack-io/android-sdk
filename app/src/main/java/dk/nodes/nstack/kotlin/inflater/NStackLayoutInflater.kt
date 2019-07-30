package dk.nodes.nstack.kotlin.inflater

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.util.NLog
import org.xmlpull.v1.XmlPullParser
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.util.*

internal class NStackLayoutInflater internal constructor(
    original: LayoutInflater,
    newContext: Context,
    cloned: Boolean
) : LayoutInflater(original, newContext) {

    private var isPrivateFactorySet = false
    private var mConstructorArgs: Field? = null

    init {
        setUpLayoutFactories(cloned)
    }

    override fun cloneInContext(newContext: Context): LayoutInflater {
        return NStackLayoutInflater(this, newContext, true)
    }

    override fun inflate(parser: XmlPullParser, root: ViewGroup?, attachToRoot: Boolean): View {
        setPrivateFactoryInternal()
        return super.inflate(parser, root, attachToRoot)
    }

    private fun setUpLayoutFactories(cloned: Boolean) {
        if (cloned) return

        if (factory2 != null && factory2 !is WrapperFactory2) {
            factory2 = factory2
        }

        if (factory != null && factory !is WrapperFactory) {
            factory = factory
        }
    }

    override fun setFactory(factory: Factory) {
        if (factory !is WrapperFactory) {
            super.setFactory(WrapperFactory(factory, this))
        } else {
            super.setFactory(factory)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun setFactory2(factory2: Factory2) {
        if (factory2 !is WrapperFactory2) {
            super.setFactory2(WrapperFactory2(factory2, this))
        } else {
            super.setFactory2(factory2)
        }
    }

    private fun setPrivateFactoryInternal() {
        // Already tried to set the factory.
        if (isPrivateFactorySet) {
            return
        }
        // Skip if not attached to an activity.
        if (context !is Factory2) {
            isPrivateFactorySet = true
            return
        }

        val setPrivateFactoryMethod = ReflectionUtils.getMethod(LayoutInflater::class.java, "setPrivateFactory")

        if (setPrivateFactoryMethod != null) {
            ReflectionUtils.invokeMethod(this, setPrivateFactoryMethod, PrivateWrapperFactory2(context as Factory2, this))
        }

        isPrivateFactorySet = true
    }

    @Throws(ClassNotFoundException::class)
    override fun onCreateView(name: String, attrs: AttributeSet): View? {
        var view: View? = null

        for (prefix in classPrefix) {
            try {
                view = createView(name, prefix, attrs)
            } catch (ignored: ClassNotFoundException) {
                // Do nothing
            }
        }

        if (view == null) {
            view = super.onCreateView(name, attrs)
        }

        return view
    }

    private fun createCustomViewInternal(view: View?, name: String, viewContext: Context, attrs: AttributeSet): View? {
        var v = view
        if (v == null && name.indexOf('.') > -1) {

            if (mConstructorArgs == null) {
                mConstructorArgs = ReflectionUtils.getField(LayoutInflater::class.java, "mConstructorArgs")
            }

            val mConstructorArgsArr = ReflectionUtils.getValue(mConstructorArgs!!, this) as Array<Any>
            val lastContext = mConstructorArgsArr[0]

            mConstructorArgsArr[0] = viewContext
            ReflectionUtils.setValue(mConstructorArgs!!, this, mConstructorArgsArr)

            try {
                v = createView(name, null, attrs)
            } catch (ignored: ClassNotFoundException) {
                // Do nothing
            } finally {
                mConstructorArgsArr[0] = lastContext
                ReflectionUtils.setValue(mConstructorArgs!!, this, mConstructorArgsArr)
            }
        }

        return v
    }

    /**
     * If all else fails then we just try to brute force the layout provided (I'm looking at you AppCompat....)
     */
    private fun doDirtyInflation(view: View?, name: String, context: Context, attrs: AttributeSet): View? {
        return view ?: inflateFromName(name, context, attrs)
    }

    private fun inflateFromName(name: String, context: Context, attrs: AttributeSet): View? {
        return try {
            val constructor: Constructor<out View>
            val clazz: Class<out View>?
            if (classLookup.containsKey(name)) {
                clazz = classLookup[name]
            } else {
                clazz = context.classLoader.loadClass(name).asSubclass(View::class.java) as Class<out View>
                classLookup[name] = clazz
            }
            constructor = clazz!!.getConstructor(*constructorSignature)
            constructor.isAccessible = true
            constructor.newInstance(context, attrs)
        } catch (e: Exception) {
            null
        }

    }

    /**
     * Take our view strip whatever values were put into the XML and then add that to our NStack Translation Library Cache
     */
    @SuppressLint("ResourceType")
    private fun processView(name: String, context: Context, view: View?, attrs: AttributeSet) {
        if (view == null) {
            NLog.d(this, "processView -> Null View Returning $name")
            return
        }

        if (name.contains("Layout")) return

        // try to pull our value from it

        var androidText: String? = null
        var androidHint: String? = null
        var androidDescription: String? = null
        var androidTextOn: String? = null
        var androidTextOff: String? = null
        var androidContentDescription: String? = null
        var appTitle: String? = null
        var appSubtitle: String? = null

        val androidAttributes = context.obtainStyledAttributes(attrs, set)

        if (androidAttributes.getText(0) != null) {
            androidText = androidAttributes.getText(0).toString()
        }
        if (androidAttributes.getText(1) != null) {
            androidHint = androidAttributes.getText(1).toString()
        }
        if (androidAttributes.getText(2) != null) {
            androidDescription = androidAttributes.getText(2).toString()
        }
        if (androidAttributes.getText(3) != null) {
            androidTextOn = androidAttributes.getText(3).toString()
        }
        if (androidAttributes.getText(4) != null) {
            androidTextOff = androidAttributes.getText(4).toString()
        }
        if (androidAttributes.getText(5) != null) {
            androidContentDescription = androidAttributes.getText(5).toString()
        }
        if (androidAttributes.getText(6) != null) {
            appTitle = androidAttributes.getText(6).toString()
        }
        if (androidAttributes.getText(7) != null) {
            appSubtitle = androidAttributes.getText(7).toString()
        }

        androidAttributes.recycle()


        // Get our typed array
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.nstack, 0, 0)
        // Custom nstack:* attributes
        var key: String?
        var text: String?
        var hint: String?
        var description: String?
        var textOn: String?
        var textOff: String?
        var contentDescription: String?
        var title: String?
        var subtitle: String?

        try {
            key = typedArray.getString(R.styleable.nstack_key)
            text = typedArray.getString(R.styleable.nstack_text)
            hint = typedArray.getString(R.styleable.nstack_hint)
            description = typedArray.getString(R.styleable.nstack_description)
            textOn = typedArray.getString(R.styleable.nstack_textOn)
            textOff = typedArray.getString(R.styleable.nstack_textOff)
            contentDescription = typedArray.getString(R.styleable.nstack_contentDescription)
            title = typedArray.getString(R.styleable.nstack_title)
            subtitle = typedArray.getString(R.styleable.nstack_subtitle)
        } finally {
            typedArray.recycle()
        }

        if (androidText != null) {
            text = androidText
        }
        if (androidHint != null) {
            hint = androidHint
        }
        if (androidDescription != null) {
            description = androidDescription
        }

        if (androidTextOn != null) {
            textOn = androidTextOn
        }
        if (androidTextOff != null) {
            textOff = androidTextOff
        }
        if (androidContentDescription != null) {
            contentDescription = androidContentDescription
        }
        if (appTitle != null) {
            title = appTitle
        }
        if (appSubtitle != null) {
            subtitle = appSubtitle
        }

        key = cleanKeyName(key)
        text = cleanKeyName(text)
        hint = cleanKeyName(hint)
        description = cleanKeyName(description)
        textOn = cleanKeyName(textOn)
        textOff = cleanKeyName(textOff)
        contentDescription = cleanKeyName(contentDescription)
        title = cleanKeyName(title)
        subtitle = cleanKeyName(subtitle)

        if (key == null &&
            text == null &&
            hint == null &&
            description == null &&
            textOn == null &&
            textOff == null &&
            contentDescription == null &&
            title == null &&
            subtitle == null) {
            NLog.d(this, "processView -> Found no valid NStack keys $name")
            return
        }

        val translationData = TranslationData(key, text, hint, description, textOn, textOff, contentDescription, title, subtitle)

        NStack.addView(view, translationData)
    }

    private fun cleanKeyName(keyName: String?): String? {
        var key: String? = keyName ?: return null

        if (key!!.startsWith("{") && key.endsWith("}")) {
            key = key.substring(1, key.length - 1)
        }

        return key
    }

    private class WrapperFactory constructor(
        private var factory: Factory,
        private var layoutInflater: NStackLayoutInflater
    ) : Factory {

        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            // Try to generate our view from our factory
            var view: View? = factory.onCreateView(name, context, attrs)
            // If this fails then we should just try to brute force
            view = layoutInflater.doDirtyInflation(view, name, context, attrs)
            // After brute forcing we should add it to the NStack View Cache
            layoutInflater.processView(name, context, view, attrs)
            return view
        }
    }

    private open class WrapperFactory2 constructor(
        protected val factory2: Factory2,
        protected var layoutInflater: NStackLayoutInflater
    ) : Factory2 {

        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            // Try to generate our view from our factory
            var view: View? = factory2.onCreateView(name, context, attrs)
            // If this fails then we should just try to brute force
            view = layoutInflater.doDirtyInflation(view, name, context, attrs)
            // After brute forcing we should add it to the NStack View Cache
            layoutInflater.processView(name, context, view, attrs)
            return view
        }

        override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
            // Try to generate our view from our factory
            var view: View? = factory2.onCreateView(parent, name, context, attrs)
            // If this fails then we should just try to brute force
            view = layoutInflater.doDirtyInflation(view, name, context, attrs)
            // After brute forcing we should add it to the NStack View Cache
            layoutInflater.processView(name, context, view, attrs)
            return view
        }
    }

    private class PrivateWrapperFactory2 constructor(
        factory2: Factory2,
        inflater: NStackLayoutInflater
    ) : WrapperFactory2(factory2, inflater) {

        override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
            return layoutInflater.createCustomViewInternal(factory2.onCreateView(parent, name, context, attrs), name, context, attrs)
        }
    }

    companion object {

        private val classPrefix = arrayOf("", "android.widget.", "android.webkit.")

        // Standard android:* attributes
        private val set = intArrayOf(
            android.R.attr.text,
            android.R.attr.hint,
            android.R.attr.description,
            android.R.attr.textOn,
            android.R.attr.textOff,
            android.R.attr.contentDescription,
            R.attr.title,
            R.attr.subtitle
        )

        private val classLookup = HashMap<String, Class<out View>>()

        private val constructorSignature = arrayOf(Context::class.java, AttributeSet::class.java)
    }
}
