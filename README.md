# kstack

## Init

Initialize library
```
KStack.setLogFunction { tag, msg -> Log.e(tag, msg) }
KStack.init(this, "BmZHmoKuU99A5ZnOByOiRxMVSmAWC2yBz3OW", "yw9go00oCWt6zPhfbdjRYXiHRWmkQZQSuRke", true)
KStack.setTranslationClass(Translation::class.java)
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

## Version control
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
