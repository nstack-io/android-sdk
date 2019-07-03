package dk.nodes.nstack.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.nodes.nstack.kotlin.inflater.NStackRootLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nStackRootLayout = NStackRootLayout(this)
        layoutInflater.inflate(R.layout.activity_main, nStackRootLayout)

        setContentView(nStackRootLayout)

        noButton.text = Translation.defaultSection.no
    }
}
