package dk.nodes.nstack.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.inflater.NStackRootLayout
import dk.nodes.nstack.kotlin.util.extensions.plusAssign
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nStackRootLayout = NStackRootLayout(this)
        layoutInflater.inflate(R.layout.activity_main, nStackRootLayout)
        setContentView(nStackRootLayout)

        NStack.setTranslation(toolbar, "rateReminder", "title", title = getString(R.string.nstack_rateReminder_title))

        messageTextView += getString(R.string.nstack_rateReminder_body)
        yesButton += getString(R.string.nstack_rateReminder_yesBtn)
        noButton += getString(R.string.nstack_rateReminder_noBtn)
    }
}
