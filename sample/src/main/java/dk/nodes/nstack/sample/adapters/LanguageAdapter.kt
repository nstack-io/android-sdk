package dk.nodes.nstack.sample.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dk.nodes.nstack.sample.R
import kotlinx.android.synthetic.main.rv_item_language.view.*
import java.util.*

class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    var locales: ArrayList<Locale> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onLocaleClicked: ((Locale) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.rv_item_language,
                parent,
                false
        )

        return LanguageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return locales.size
    }

    override fun onBindViewHolder(holder: LanguageViewHolder?, position: Int) {
        val locale = locales[position]
        holder?.bind(locale)
    }

    inner class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(locale: Locale) {
            itemView.rvItemLanguageTvMain.text = locale.displayLanguage
            itemView.rvItemLanguageCard.setOnClickListener {
                onLocaleClicked?.invoke(locale)
            }
        }
    }
}