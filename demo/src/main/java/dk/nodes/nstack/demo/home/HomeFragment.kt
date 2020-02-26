package dk.nodes.nstack.demo.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.kotlin.NStack
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NStack.setTranslation(messageTextView, getString(R.string.nstack_home_message))
    }
}