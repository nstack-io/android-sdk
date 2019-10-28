package dk.nodes.nstack.demo.more

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.kotlin.NStack
import kotlinx.android.synthetic.main.fragment_more.*

class MoreFragment : Fragment(R.layout.fragment_more) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedbackButton.setOnClickListener {
            NStack.Feedback.show(view.context)
        }
    }
}