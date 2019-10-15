package dk.nodes.nstack.kotlin.data.terms

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dk.nodes.nstack.kotlin.models.Term
import dk.nodes.nstack.kotlin.util.Constants
import dk.nodes.nstack.kotlin.util.Preferences

internal class TermsRepository(
        private val preferences: Preferences,
        private val gson: Gson
) {

    private val gsonLatestTermsType = object : TypeToken<List<Term>>() {}.type

    fun setLatestTerms(terms: List<Term>) {
        preferences.saveString(
                key = Constants.spk_nstack_latest_terms,
                value = gson.toJson(terms)
        )
    }

    fun getLatestTerms(): List<Term> {
        return try {
            gson.fromJson(
                    preferences.loadString(key = Constants.spk_nstack_latest_terms),
                    gsonLatestTermsType
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}