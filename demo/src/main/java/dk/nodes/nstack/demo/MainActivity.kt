package dk.nodes.nstack.demo

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.inflater.NStackBaseContext
import dk.nodes.nstack.kotlin.util.extensions.plusAssign
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        messageTextView += getString(R.string.nstack_test_title)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(NStackBaseContext(newBase))
    }
}
