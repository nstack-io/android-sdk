package dk.nodes.nstack.kotlin.inflater;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dk.nodes.nstack.R;
import dk.nodes.nstack.kotlin.NStack;

class NStackLayoutInflater extends LayoutInflater {
    private static final String TAG = "NStackLayoutInflater";

    private static final String[] classPrefix = {
            "android.widget.",
            "android.webkit."
    };

    private boolean mSetPrivateFactory = false;
    private Field mConstructorArgs = null;

    protected NStackLayoutInflater(LayoutInflater original, Context newContext, final boolean cloned) {
        super(original, newContext);
        setUpLayoutFactories(cloned);
    }

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new NStackLayoutInflater(this, newContext, true);
    }

    @Override
    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) {
        setPrivateFactoryInternal();
        return super.inflate(parser, root, attachToRoot);
    }

    private void setUpLayoutFactories(boolean cloned) {
        if (cloned) return;

        if (getFactory2() != null && !(getFactory2() instanceof WrapperFactory2)) {
            setFactory2(getFactory2());
        }

        if (getFactory() != null && !(getFactory() instanceof WrapperFactory)) {
            setFactory(getFactory());
        }
    }

    @Override
    public void setFactory(Factory factory) {
        if (!(factory instanceof WrapperFactory)) {
            super.setFactory(new WrapperFactory(factory, this));
        } else {
            super.setFactory(factory);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setFactory2(Factory2 factory2) {
        if (!(factory2 instanceof WrapperFactory2)) {
            super.setFactory2(new WrapperFactory2(factory2, this));
        } else {
            super.setFactory2(factory2);
        }
    }

    private void setPrivateFactoryInternal() {
        // Already tried to set the factory.
        if (mSetPrivateFactory) return;
        // Skip if not attached to an activity.
        if (!(getContext() instanceof Factory2)) {
            mSetPrivateFactory = true;
            return;
        }

        final Method setPrivateFactoryMethod = ReflectionUtils.getMethod(LayoutInflater.class, "setPrivateFactory");

        if (setPrivateFactoryMethod != null) {
            ReflectionUtils.invokeMethod(this, setPrivateFactoryMethod, new PrivateWrapperFactory2((Factory2) getContext(), this));
        }

        mSetPrivateFactory = true;
    }

    @Override
    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        View view = null;

        for (String prefix : classPrefix) {
            try {
                view = createView(name, prefix, attrs);
            } catch (ClassNotFoundException ignored) {
                // Do nothing
            }
        }

        if (view == null) {
            view = super.onCreateView(name, attrs);
        }

        return view;
    }

    private View createCustomViewInternal(View parent, View view, String name, Context viewContext, AttributeSet attrs) {
        if (view == null && name.indexOf('.') > -1) {

            if (mConstructorArgs == null) {
                mConstructorArgs = ReflectionUtils.getField(LayoutInflater.class, "mConstructorArgs");
            }

            final Object[] mConstructorArgsArr = (Object[]) ReflectionUtils.getValue(mConstructorArgs, this);
            final Object lastContext = mConstructorArgsArr[0];

            mConstructorArgsArr[0] = viewContext;
            ReflectionUtils.setValue(mConstructorArgs, this, mConstructorArgsArr);

            try {
                view = createView(name, null, attrs);
            } catch (ClassNotFoundException ignored) {
                // Do nothing
            } finally {
                mConstructorArgsArr[0] = lastContext;
                ReflectionUtils.setValue(mConstructorArgs, this, mConstructorArgsArr);
            }
        }
        return view;
    }

    private void processView(String name, Context context, View view, AttributeSet attrs) {
        if (view == null) {
            Log.i(TAG, "processView -> Null View Returning " + name);
            return;
        }
        // Get our typed array
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.nstack, 0, 0);
        // try to pull our value from it
        String nstackKey;

        try {
            nstackKey = typedArray.getString(R.styleable.nstack_key);
        } finally {
            typedArray.recycle();
        }

        if (nstackKey == null || nstackKey.isEmpty()) {
            Log.i(TAG, "processView -> Invalid NStack Key Returning " + name);
            return;
        }

        NStack.INSTANCE.getViewMap().put(new WeakReference<>(view), nstackKey);
    }

    private static class WrapperFactory implements Factory {
        Factory factory;
        NStackLayoutInflater layoutInflater;

        WrapperFactory(Factory factory, NStackLayoutInflater layoutInflater) {
            this.factory = factory;
            this.layoutInflater = layoutInflater;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            View view = factory.onCreateView(name, context, attrs);
            layoutInflater.processView(name, context, view, attrs);
            return view;
        }
    }

    private static class WrapperFactory2 implements Factory2 {
        protected final Factory2 factory2;
        NStackLayoutInflater layoutInflater;

        public WrapperFactory2(Factory2 factory2, NStackLayoutInflater layoutInflater) {
            this.factory2 = factory2;
            this.layoutInflater = layoutInflater;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            View view = factory2.onCreateView(name, context, attrs);
            layoutInflater.processView(name, context, view, attrs);
            return view;
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            View view = factory2.onCreateView(parent, name, context, attrs);
            layoutInflater.processView(name, context, view, attrs);
            return view;
        }
    }

    private static class PrivateWrapperFactory2 extends WrapperFactory2 {
        PrivateWrapperFactory2(Factory2 factory2, NStackLayoutInflater inflater) {
            super(factory2, inflater);
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            return layoutInflater.createCustomViewInternal(parent, factory2.onCreateView(parent, name, context, attrs), name, context, attrs);
        }
    }

}
