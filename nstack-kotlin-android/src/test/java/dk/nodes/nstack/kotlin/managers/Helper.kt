package dk.nodes.nstack.kotlin.managers

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.File

object Helper {

    /**
     * Sets which response the [MockWebServer] should return when a request is made
     */
    fun MockWebServer.setResponse(fileName: String, responseCode: Int = 200) {
        enqueue(
                MockResponse()
                        .setResponseCode(responseCode)
                        .setBody(getFileAsString(fileName))
        )
    }

    /**
     * The the file in the [filePath] and return its content as a [String]
     */
    fun getFileAsString(filePath: String): String {
        val uri = ClassLoader.getSystemResource(filePath)
        val file = File(uri.path)
        return String(file.readBytes())
    }
}