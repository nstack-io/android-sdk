![nStack Badge](https://img.shields.io/maven-central/v/dk.nodes.nstack/nstack-kotlin.svg)
<p align="center">
  <img src="NStack_Logo.png?raw=true" alt="NStack"/>
</p>

# NStack Kotlin
NStackSDK is the companion software development kit to the NStack backend.
See the [NStack documentation](https://nstack-io.github.io/documentation/index.html) for more information about NStack


## Quick  Setup

### 📦 Installation

1. Add NStack gradle plugin to your top-level `build.gradle` file located in your project folder
```groovy
buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath "dk.nodes.nstack:translation:3.2.5"
    }
}
```
2. Add NStack SDK dependency to your app module
```groovy
translation {
    appId = "replace with NStack Application id"
    apiKey = "replace with NStack Rest API key"
    acceptHeader = "en-GB"
}

dependencies {
    implementation "dk.nodes.nstack:nstack-kotlin:3.2.5"
}
```
3. Create an empty `Translation.java` file in the app module
4. Create an empty `RateReminderActions.kt` file in the app module
4. Sync your project with gradle files. After synchronisation is complete, you can start using the SDK

### ⚒ Configuration
In order to use NStack SDK you have to initialize and configure it first.

In order to connect NStack API with your application you will need `ApplicationId`, `REST API Key`. For more information how to get these keys checkout our  [Getting Start Guide](https://nstack-io.github.io/documentation/docs/guides/getting-started.html).

Put these keys as meta-data in your `AndroidManifest.xml` like so:
```xml
<application>
       <meta-data
           android:name="dk.nodes.nstack.appId"
           android:value="your application Id"
           tools:replace="android:value" />


       <meta-data
           android:name="dk.nodes.nstack.apiKey"
           android:value="your REST API key"
           tools:replace="android:value" />

       <meta-data
          android:name="dk.nodes.nstack.env"
          android:value="staging"
          tools:replace="android:value" />

          ....

</application>
```

> You can also put these values into your `build.gradle` and use placeholders in the manifest
```groovy
android {
    defaultConfig {
        manifestPlaceholders = [
            appId : translation.appId,
            apiKey: translation.apiKey
        ]
    }
}
```

 Best place to initialise SDK will be in you Application `onCreate()` method as it requires your's application `Context`. `Application` is a the class for maintaining global application state. Heres a basic SDK initiation example

```kotlin
class MyApplication : Application() {
   override fun onCreate(){
     super.onCreate()
     // Specify your Translation class where translation string will be stored
     NStack.translationClass = Translation::class.java
     // initilize the SDK
     NStack.init(this, BuildConfig.DEBUG)
   }
}
```
There also **optional** parameters you could make use of while using NStack SDK:

```kotlin
NStack.debugMode = true - Enables debug mode for the library (Outputs messages to log)
NStack.setRefreshPeriod(60, TimeUnit.MINUTES) - Allows you to set the period for how often NStack should check for updates
```
> Warning: In almost every instance you want to set these optional methods before NStack is initialized

## Activity Setup
Add this to which ever activity you are trying to use NStack in

```kotlin
override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(NStackBaseContext(newBase))
}
```

Pretty simple you just need to wrap your `BaseContext` with our custom wrapper

## App Open
App Open enables apps to pull info from several features in one API request, you can learn
more about it [here](https://nstack-io.github.io/documentation/docs/app-open.html).

Minimal setup requires calling following function:
```kotlin
GlobalScope.launch {
    when (val result = withContext(Dispatchers.IO) {
        NStack.appOpen()
    }) {
        is Result.Success -> { }
        is Result.Error -> { }
    }
}
```

## Language Selection

```kotlin
NStack.availableLanguages
```
Provides an `Arraylist<Locale>` of all available languages

```kotlin
NStack.languages
```
Provides an `HashMap<Locale, JSONObject>` of all available languages as the key and the language json object as the value

```kotlin
NStack.language = selectedLocale
```

Using any of the provided locales you are able to select a language simply by setting the `language` variable in NStack

```kotlin
NStack.setLanguageByString("en-gb")
```

Allows you to set the language by string the format must follow either the `language-country` or `language_country` format otherwise it just won't do anything

## Xml Translation

Starting in version `2.1.0` NStack-Kotlin now supports XML based translations embedded in the android namespace

```XML
android:text="{sectionName_keyName}"
android:hint="{sectionName_keyName}"
android:description="{sectionName_keyName}"
android:textOn="{sectionName_keyName}"
android:textOff="{sectionName_keyName}"
android:contentDescription="{sectionName_keyName}"
```

Method from 2.0.2+ is still supported as follows:

```XML
xmlns:nstack="http://schemas.android.com/apk/res-auto"
tools:ignore="MissingPrefix"
```

Before starting with the XML translations be sure to add the following block to the root of whatever layout you're using.

```XML
nstack:key="sectionName_keyName"
nstack:text="sectionName_keyName"
nstack:hint="sectionName_keyName"
nstack:description="sectionName_keyName"
nstack:textOn="sectionName_keyName"
nstack:textOff="sectionName_keyName"
nstack:contentDescription="sectionName_keyName"
```

The following field should be used to set the nstack key `nstack:key="keyGoesHere"`

When entering the key the following format should be used `sectionName_keyName`

If you're using the NStack Gradle plugin a `nstack_keys.xml` should be generated containing all available keys it's suggested that you reference those keys when using this feature

#### Queuing Manual Translations

Once you have that setup you can trigger the translation via the following method

```kotlin
NStack.translate()
```

> **Note: Running this command is optional as the views get their translation added as they are added**

#### Clearing View Cache

If for whatever reason you need to clear the translate view cache you can trigger that view the following method

```kotlin
NStack.clearViewCache()
```

## Language Listeners
If you interested in locale changes events NStack allows you set up a `LanguageListener` like follows:
```kotlin
NStack.addLanguageChangeListener{ locale: Locale ->
  // Your code
}
```

Adds a listener to NStack that will trigger every time the language is changed (returns the new locale)


```kotlin
NStack.addLanguagesChangeListener {
}
```

This listener should trigger every time the available languages change

## N-Meta Header
Use like this:
```kotlin
.addInterceptor(NMetaInterceptor("staging"))
```
Where "staging" is a string you pass in a buildconfig flag or something similar

## Dependencies
- okhttp 3.8.0

#### Snapshots
If you need to target SNAPSHOT releases you depend on following repositories:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}

dependencies {
    implementation "dk.nodes.nstack:nstack-kotlin:x.x.x-SNAPSHOT"
}
```
