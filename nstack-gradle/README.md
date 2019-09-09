![nStack Badge](https://maven-badges.herokuapp.com/maven-central/dk.nodes.nstack/translation/badge.svg)

# nstack-gradle

Gradle plugin for [nstack.io](https://nstack.io) to generate various project files such as:

+ Translation.java (Model class for using translations)
+ nstack_keys.xml (Strings resouces containing a list of all NStack keys to be used with the NStack Kotlin plugin)
+ translations_{index}_{locale}.json files in assets folder. Each of them contains translations for corresponding locale.

### Setup

Add this to your **Project** build.gradle:
```groovy
buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath "dk.nodes.nstack:translation:${nstack_plugin_version}"
    }
}
```

Add this to your **Module** build.gradle:

```groovy
apply plugin: 'dk.nstack.translation.plugin'

translation {
    appId = "<generated app id from nstack.io>"
    apiKey = "<generated app key from nstack.io>"
    acceptHeader = "da-dk" // Accept header for which langauge we are selecting
    autoRunUpdate = true // Should the gradle task auto update the translation assets/keys
}
```

### Run

Find the **generateTranslationClass** gradle task and run it. Located in :<project>/nstack.
