package dk.nstack.kotlin.plugin

class Util {

    static String getTextFromUrl(String url) {
        try {
            def inputFile = new URL(url)
            return inputFile.getText(requestProperties: [
                    'Accept-Language' : TranslationPlugin.project.translation.acceptHeader,
                    'X-Application-Id': TranslationPlugin.project.translation.appId,
                    'X-Rest-Api-Key'  : TranslationPlugin.project.translation.apiKey,
                    'N-Meta'          : 'androidstudio;debug;1.0;1.0;gradleplugin'
            ])
        } catch (IOException exception) {
            Log.error("Fetching content from $url failed.")
            exception.println()
            return ""
        }
    }

    static String getTextFromUrlWithErrorResponse(String url) {
        try {
            url.toURL().openConnection().with { conn ->
                setRequestProperty("Accept-Language", TranslationPlugin.project.translation.acceptHeader)
                setRequestProperty("X-Application-Id", TranslationPlugin.project.translation.appId)
                setRequestProperty("X-Rest-Api-Key", TranslationPlugin.project.translation.apiKey)
                setRequestProperty("N-Meta", 'androidstudio;debug;1.0;1.0;gradleplugin')
                switch (responseCode) {
                    case 404:
                        Log.info("Rate reminder does not seem to be set up, ignoring RateReminderActions...")
                        return ""
                    case [200, 201]:
                        conn.content.withReader { r ->
                            return r.text
                        }
                        break
                    default:
                        return ""
                }
            }
        } catch (IOException exception) {
            Log.error("Fetching content from $url failed.")
            exception.println()
            return ""
        }
    }
}
