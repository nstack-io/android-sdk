package dk.nstack.kotlin.plugin

class Util {

    static String getTextFromUrl(String url) throws IOException {
        def inputFile = new URL(url)

        return inputFile.getText(requestProperties: [
                'Accept-Language' : TranslationPlugin.project.translation.acceptHeader,
                'X-Application-Id': TranslationPlugin.project.translation.appId,
                'X-Rest-Api-Key'  : TranslationPlugin.project.translation.apiKey,
                'N-Meta'          : 'androidstudio;debug;1.0;1.0;gradleplugin'
        ])
    }

    static String getTextFromUrlWithErrorResponse(String url) {
        url.toURL().openConnection().with { conn ->
            setRequestProperty("Accept-Language", TranslationPlugin.project.translation.acceptHeader)
            setRequestProperty("X-Application-Id", TranslationPlugin.project.translation.appId)
            setRequestProperty("X-Rest-Api-Key", TranslationPlugin.project.translation.apiKey)
            setRequestProperty("N-Meta", 'androidstudio;debug;1.0;1.0;gradleplugin')
            if (responseCode < 200 || responseCode >= 300) {
                throw new RuntimeException("$url: $responseCode - $responseMessage")
            }
            conn.content.withReader { r ->
                return r.text
            }
        }
    }
}
