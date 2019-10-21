package dk.nodes.nstack.demo.terms

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.demo.Translation
import kotlinx.android.synthetic.main.fragment_terms.*

const val TERMS_ID = 3L

class TermsFragment : Fragment(R.layout.fragment_terms) {

    private lateinit var viewModel: TermsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[TermsViewModel::class.java]
        viewModel.viewState.observe(this, Observer(this::showViewState))
        viewModel.loadTerms(TERMS_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        termsAcceptButton.setOnClickListener {
            viewModel.acceptTerms()
        }

        val colorAccent = ContextCompat.getColor(view.context, R.color.colorAccent)
        swipeRefreshLayout.setColorSchemeColors(colorAccent)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadTerms(TERMS_ID)
        }
    }

    private fun showViewState(state: TermsViewState) {
        swipeRefreshLayout.isRefreshing = state.isLoading
        emptyView.isVisible = !state.isLoading && state.termsContent.isNullOrEmpty()
        termsAcceptButton.isVisible = state.isAccepted != null

        state.termsName?.let {
            toolbar.title = it
        }

        state.termsContent?.let {
            termsContentTextView.text = HtmlCompat.fromHtml(it, FROM_HTML_MODE_LEGACY)
        }

        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        state.isAccepted?.let {
            termsAcceptButton.showAccepted(it)
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

    private var View.isVisible: Boolean
        get() = visibility == View.VISIBLE
        set(value) {
            visibility = if (value) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
}
