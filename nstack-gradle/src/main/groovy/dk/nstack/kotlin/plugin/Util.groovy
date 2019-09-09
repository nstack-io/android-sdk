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
}