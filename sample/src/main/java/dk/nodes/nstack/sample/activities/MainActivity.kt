package dk.nodes.nstack.sample.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.sample.R
import dk.nodes.nstack.sample.adapters.LanguageAdapter
import dk.nodes.nstack.sample.models.Translation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val adapter = LanguageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupListeners()
        setupAdapter()
        setupTranslations()
        getLanguages()
    }

    private fun setupListeners() {
        NStack.onLanguageChanged = {
            setupTranslations()
        }

        NStack.onLanguagesChanged = {
            getLanguages()
        }
    }

    private fun setupAdapter() {
        mainRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mainRv.adapter = adapter

        adapter.onLocaleClicked = {
            NStack.language = it
        }
    }

    private fun setupTranslations() {
        mainTv1.text = Translation.defaultSection.connectionError
        mainTv2.text = Translation.defaultSection.sessionExpired
        mainTv3.text = Translation.defaultSection.unknownError
        mainTv4.text = Translation.defaultSection.reminderAlertBody
        mainTv5.text = Translation.defaultSection.welcomeDialogMessage
    }

    private fun getLanguages() {
        adapter.locales = NStack.availableLanguages
    }
}
