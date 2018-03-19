![nStack Badge](https://maven-badges.herokuapp.com/maven-central/dk.nodes.nstack/nstack-kotlin/badge.svg)

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

## Setup

Add this to gradle:
```groovy
compile "dk.nodes.nstack:nstack-kotlin:x.x.x"
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
Version control will send the result from `appOpen` to the listener for the application to handle

Now it's up to the app to decide how you want to handle the app update status (Meaning you must create your own dialog and what not)

**Note: You should set this before `appOpen`**

```
NStack.onAppUpdateListener = { appUpdate ->
    when (appUpdate.state) {
        AppUpdateState.NONE      -> {
            // Do nothing because there is no update
        }
        AppUpdateState.UPDATE    -> {
            // Show a user a dialog that is dismissible
        }
        AppUpdateState.FORCE     -> {
            // Show the user an undismissable dialog
        }
        AppUpdateState.CHANGELOG -> {
            // Show change log (Not yet implemented because its never used)
        }
    }
}
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

## Xml Translation

Starting in version `2.0.2` NStack-Kotlin now supports XML based translations

```
   <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        nstack:key="@string/nstack_attachments_lockedMessage" />
```

The following field should be used to set the nstack key `nstack:key="{keyGoesHere}"`

When entering the key the following format should be used `sectionName_keyName`

If you're using the NStack Gradle plugin a `nstack_keys.xml` should be generated containing all available keys it's suggested that you reference those keys when using this feature

Once you have that setup you can trigger the translation via the following method

```
NStack.translate()
```

**Note: Running this command is optional as the views get their translation added as they are added**

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
