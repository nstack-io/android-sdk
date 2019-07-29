package dk.nodes.nstack.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.inflater.NStackRootLayout
import dk.nodes.nstack.kotlin.util.plusAssign
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nStackRootLayout = NStackRootLayout(this)
        layoutInflater.inflate(R.layout.activity_main, nStackRootLayout)

        setContentView(nStackRootLayout)

        NStack.setTranslation(toolbar, "test", "title", title = "test_title")

        messageTextView += "test_message"
        yesButton += "default_yes"
        noButton += "default_no"
    }
}
