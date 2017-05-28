# KStack
Minimalistic reimplementation of NStack in kotlin with coroutines.
Does a decent job of showcasing a lot of language features.

Does not do changelog or ratereminders (but then again who use them?) and probably other
stuff I forgot or didn't care about.

Since it uses coroutine jobs which checks and wait for each other its safe to
fire version control immediately after appopen.

**Do NOT call versionControl inside the appOpen callback**

## Init

Initialize library
```
KStack.init(this, "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke")
KStack.setTranslationClass(Translation::class.java)
```

## Init in debug

Initialize the library in debug mode (if its a debug build):
```
KStack.setLogFunction { tag, msg -> Log.e(tag, msg) }
KStack.init(this, "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke", BuildConfig.DEBUG)
KStack.setTranslationClass(Translation::class.java)
```

Debug can mode can also be enabled post initialization by setting the debug property:
```
KStack.debug = true
```

## AppOpen

Minimal:
```
KStack.appOpen()
```

If you care about the outcome or want to run code afterwards:
```
KStack.appOpen({success -> Log.e("debug", "appopen success = $success") })
```

## VersionControl
Version control example. Version control automatically waits for appopen. So if you never call
appopen, it will never run
```
KStack.versionControl(this@MainActivity, {type, builder ->
    when(type)
    {
        UpdateType.UPDATE -> builder?.show()
        UpdateType.FORCE_UPDATE -> {
            builder?.setOnDismissListener { finish() }
            builder?.show()
        }
        else -> {
        }
    }
})
```

## N-Meta Header
Use like this:
```
.addInterceptor(NMetaInterceptor("staging"))
```
Where "staging" is a string you pass in a buildconfig flag or something similar

## Dependencies
- appcompat
- okhttp 3.8.0