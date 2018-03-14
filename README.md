# nStack Kotlin 2.0

Updated version of nStack implemented in a more kotlin friendly way

Developed by 
- @STPE
- @BRES

## Init

Initialize library
```
NStack.translationClass = Translation::class.java
NStack.init(this)
```

##### Optional Parameters

` NStack.debugMode = true` - Enables debug mode for the library (Outputs messages to log)
` NStack.setRefreshPeriod(60, TimeUnit.MINUTES)` - Allows you to set the period for how often NStack should check for updates

*Warning: In almost every instance you want to set these optional methods before NStack is initialized*

## AppOpen

Minimal:
```

NStack.appOpen()
```

If you care about the outcome or want to run code afterwards:
```
NStack.appOpen { success ->  }
```

## VersionControl
Version control will now sent the last cached value for the updater or wait until the app opened to notify the app
whether it should update

Now it's up to the app to decide how you want to handle the app update status (Meaning you must create your own dialog and what not)
```
NStack.onAppUpdateListener = { appUpdate -> }
```

## Language Selection

```
NStack.availableLanguages
```
Provides an `Arraylist<Locale>` of all available languages

```
NStack.languages
```
Provides an `HashMap<Locale, JSONObject>` of all available languages as the key and the language json object as the value

```
NStack.language = selectedLocale
```

Using any of the provided locales you are able to select a language simply by setting the `language` variable in NStack

```
NStack.setLanguageByString("en-gb")
```

Allows you to set the language by string the format must follow either the `language-country` or `language_country` format otherwise it just won't do anything

## Language Listeners

```
NStack.addLanguageChangeListener{ locale ->
}
```

Adds a listener to NStack that will trigger every time the language is changed (returns the new locale)


```
NStack.addLanguagesChangeListener {
}
```

This listener should trigger every time the available languages change

## N-Meta Header
Use like this:
```
.addInterceptor(NMetaInterceptor("staging"))
```
Where "staging" is a string you pass in a buildconfig flag or something similar

## Dependencies
- okhttp 3.8.0
