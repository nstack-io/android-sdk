package dk.nstack.kotlin.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import groovy.json.StringEscapeUtils
import groovy.xml.MarkupBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

class TranslationPlugin implements Plugin<Project> {

    public static final String GROUP_NAME = "nstack"
    public static
    final String JAVA_SOURCE_PATH = "${File.separator}src${File.separator}main${File.separator}java"
    public static final String TRANSLATION_FILE_NAME = "Translation.java"
    public static final String RATE_REMINDER_ACTIONS_FILE_NAME = "RateReminderActions.kt"

    def pathPrefix = ""
    public static Project project = null


    void apply(Project project) {
        this.project = project

        project.setDescription("Gradle extensions for nstack.io translations")

        def hasApp = project.plugins.withType(AppPlugin)

        def hasLib = project.plugins.withType(LibraryPlugin)

        if (!hasApp && !hasLib) {
            throw new IllegalStateException("NSTACK GRADLE PLUGIN: 'android' or 'android-library' plugin required.")
        }

        // Add the extension object

        project.extensions.create("translation", TranslationExtension)

        project.task('generateTranslationClass') {
            group GROUP_NAME

            // If we have auto run update then we should run it :D

            doLast {
                ensureProjectProperties()
                generateTranslationClass()
            }
        }

        project.task('generateRateReminderActions') {
            group GROUP_NAME

            doLast {
                ensureProjectProperties()
                generateRateReminderActions()
            }
        }

        project.afterEvaluate {
            if (project.translation.autoRunUpdate) {
                generateTranslationClass()
                generateRateReminderActions()
            }
        }
    }

    private static void ensureProjectProperties() {
        if (project.hasProperty("nstackAppId")) {
            Log.info("Gradle task had custom appId set: " + project.getProperties().get("nstackAppId"))
            project.translation.appId = project.getProperties().get("nstackAppId")
        }

        if (project.hasProperty("nstackApiKey")) {
            Log.info("Gradle task had custom apiKey set: " + project.getProperties().get("nstackApiKey"))
            project.translation.apiKey = project.getProperties().get("nstackApiKey")
        }
    }

    private static void generateRateReminderActions() {
        getRateReminderActionsPath()
        def packageName = project.translation.modelPath
        def enumString = RateReminderActionsGenerator.generateActions(packageName)
        def enumFile = new File(project.translation.classPath)
        if (!enumFile.exists()) {
            enumFile = new File('app/' + project.translation.classPath)
        }
        enumFile.write(enumString)
    }

    private void generateTranslationClass() {
        String acceptHeader = project.translation.acceptHeader

        Log.info("Fetching: " + acceptHeader)

        Map translations = AssetManager.saveAllTranslationsToAssets()

        getTranslationPath()
        Map languageObject = getTranslationForLocale(translations, acceptHeader)
        generateStringsResource(languageObject)
        generateJavaClass(languageObject)
    }

    private static Map getTranslationForLocale(Map translations, String string) {
        Log.info("Searching for locale -> $string")

        String[] availableLanguages = translations.keySet()

        if (translations.containsKey(string)) {
            return translations[string]
        } else {
            throw new Exception("Unable to locate a translation for $string, please check acceptHeader in project build.gradle \n\rCurrent available locales $availableLanguages")
        }
    }

    /**
    * Find the path for the Translation.java file
    * This file is where we generate main class and inner classes from the JSON file from nstack
    * */

    void getTranslationPath() {
        String searchName = TRANSLATION_FILE_NAME.toLowerCase()
        String classFilePath = null

        File searchPath = new File(project.projectDir, JAVA_SOURCE_PATH)

        searchPath.eachFileRecurse { file ->
            String filePath = file.path
            if (filePath.toLowerCase().contains(searchName)) {
                Log.debug("Found File -> " + filePath)
                classFilePath = filePath
            }
        }

        if (classFilePath == null) {
            throw Exception("Unable to locate translation file")
        }

        String possibleModelPath = classFilePath

        int startOfIndex = possibleModelPath.indexOf(JAVA_SOURCE_PATH) + JAVA_SOURCE_PATH.size() + 1
        int endOfIndex = possibleModelPath.indexOf("Translation.java") - 1
        possibleModelPath = possibleModelPath.substring(startOfIndex, endOfIndex)
        possibleModelPath = possibleModelPath.replace(File.separator, ".")

        project.translation.modelPath = possibleModelPath
        project.translation.classPath = classFilePath
    }

    private static String getRateReminderActionsPath() {
        String searchName = RATE_REMINDER_ACTIONS_FILE_NAME.toLowerCase()
        String classFilePath = null

        File searchPath = new File(project.projectDir, JAVA_SOURCE_PATH)

        searchPath.eachFileRecurse { file ->
            String filePath = file.path
            if (filePath.toLowerCase().contains(searchName)) {
                Log.debug("Found File -> " + filePath)
                classFilePath = filePath
            }
        }

        if (classFilePath == null) {
            throw Exception("Unable to locate rate reminder actions file")
        }

        String possibleModelPath = classFilePath

        int startOfIndex = possibleModelPath.indexOf(JAVA_SOURCE_PATH) + JAVA_SOURCE_PATH.size() + 1
        int endOfIndex = possibleModelPath.indexOf(RATE_REMINDER_ACTIONS_FILE_NAME) - 1
        possibleModelPath = possibleModelPath.substring(startOfIndex, endOfIndex)
        possibleModelPath = possibleModelPath.replace(File.separator, ".")

        project.translation.modelPath = possibleModelPath
        project.translation.classPath = classFilePath
    }

    /**
    * Generate our Translation.java file to project.translation.classPath
    * */
    void generateJavaClass(Map json) {

        def translationsFile = new File(project.translation.classPath)

        if (!translationsFile.exists()) {
            println "Java class does not exist, or path is wrong: " + pathPrefix + project.translation.classPath
            println "pathPrefix: " + pathPrefix
            println "classPath: " + project.translation.classPath
            pathPrefix = 'app/'
            translationsFile = new File(pathPrefix + project.translation.classPath)
        }

        def translationClassString = "package ${project.translation.modelPath};\n\n"
        translationClassString += "/**\n" +
                " * Created by nstack.io gradle translation plugin\n" +
                " * Built from Accept Header: ${project.translation.acceptHeader} \n" +
                " */\n\n"
        translationClassString += "public class Translation {\n"
        json.each {
            k, v ->
                if (v instanceof String) {
                    translationClassString += "\tpublic static String ${k} = \"${StringEscapeUtils.escapeJava(v).replace("'", "\\'")}\";\n";
                } else if (v instanceof Object) {
                    // Default is a reserved keyword
                    if (k == "default") {
                        k = "defaultSection"
                    }
                    translationClassString += generateInnerClass(k, v)
                }
        }
        translationClassString += "}\n"
        translationsFile.write(translationClassString)
    }

    /**
    *
    * @param className
    * @param data
    * @return String Inner static class with key/value strings
    */
    String generateInnerClass(className, data) {
        def innerClass = "\tpublic final static class ${className} {\n"

        data.each {
            k, v ->
                innerClass += "\t\tpublic static String ${k} = \"${StringEscapeUtils.escapeJava(v).replace("'", "\\'")}\";\n";
        }

        innerClass += "\t}\n"

        return innerClass
    }

    /**
    * Write translation data to xml as a strings resource file
    * @param json Result object of JsonSlurper parsing
    * @param project Reference to project scope
    */

    static void generateStringsResource(Map jsonSection) {
        def sw = new StringWriter()
        def xml = new MarkupBuilder(sw)

        xml.resources() {
            jsonSection.each {
                String i, j ->
                    j.each {
                        String k, String v ->
                            string(name: "nstack_${i.trim()}_${k.trim()}", formatted: "false", "{${i}_${k}}")
                    }
            }
        }

        def stringsFile = new File(project.projectDir, project.translation.stringsPath)

        stringsFile.write(sw.toString())
    }
}
