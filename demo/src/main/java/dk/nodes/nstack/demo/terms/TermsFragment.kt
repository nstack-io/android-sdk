package dk.nodes.nstack.demo.terms

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.demo.Translation
import kotlinx.android.synthetic.main.fragment_terms.*

class TermsFragment : Fragment(R.layout.fragment_terms) {

    private lateinit var viewModel: TermsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[TermsViewModel::class.java]
        viewModel.viewState.observe(this, Observer(this::showViewState))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadTerms(3)

        termsAcceptButton.setOnClickListener {
            viewModel.acceptTerms()
        }
    }

    private fun showViewState(state: TermsViewState) {
        loadingView.setVisibleOrGone(state.isLoading)
        contentView.setVisibleOrGone(!state.isLoading && !state.termsContent.isNullOrEmpty())
        emptyView.setVisibleOrGone(!state.isLoading && state.termsContent.isNullOrEmpty())

        termsAcceptButton.showAccepted(state.isAccepted)

        state.termsContent?.let {
            termsContentTextView.text = HtmlCompat.fromHtml(it, FROM_HTML_MODE_LEGACY)
        }

        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun Button.showAccepted(isAccepted: Boolean) {
        if (isAccepted) {
            text = Translation.terms.acceptedLabel
            isEnabled = false
        } else {
            text = Translation.terms.acceptLabel
            isEnabled = true
        }
    }

    private fun View.setVisibleOrGone(isVisible: Boolean) {
        visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}