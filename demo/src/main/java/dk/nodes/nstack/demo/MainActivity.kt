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

        NStack.setTranslation(
            toolbar,
            nstackKey = getString(R.string.nstack_test_title),
            title = "test_title"
        )

        messageTextView += getString(R.string.nstack_test_message)
        yesButton += getString(R.string.nstack_default_yes)
        noButton += getString(R.string.nstack_default_no)
    }
}
