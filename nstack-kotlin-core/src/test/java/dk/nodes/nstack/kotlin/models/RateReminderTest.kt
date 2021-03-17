package dk.nodes.nstack.kotlin.models

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Test

class RateReminderTest {

    @Test
    fun testParsing() {
        val raw = """
            {
              "data": {
                "id": 30,
                "points_to_trigger": 100,
                "days_delay_on_skip": 14,
                "localization": {
                  "title": "Title",
                  "body": "Body",
                  "yesBtn": "Yes",
                  "laterBtn": "Later",
                  "noBtn": "No"
                },
                "points": 100
              }
            }
        """.trimIndent()
        val gson = Gson()
        val jsonObject = gson.fromJson(raw, JsonObject::class.java)!!
        val rateReminder = RateReminder2.parse(jsonObject)!!
        assert(rateReminder.id == 30)
        assert(rateReminder.title == "Title")
        assert(rateReminder.yesButton == "Yes")
        assert(rateReminder.noButton == "No")
        assert(rateReminder.laterButton == "Later")
    }
}
