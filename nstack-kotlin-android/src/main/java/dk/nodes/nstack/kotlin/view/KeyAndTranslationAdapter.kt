package dk.nodes.nstack.kotlin.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.models.local.KeyAndTranslation

internal class KeyAndTranslationAdapter(
    private val list: List<KeyAndTranslation>,
    var callback: ((KeyAndTranslation) -> Unit)? = null
) : RecyclerView.Adapter<KeyAndTranslationAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_section_key,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val sectionKeyTv: TextView = itemView.findViewById(R.id.sectionKeyTv)

        @SuppressLint("SetTextI18n")
        fun bind(keyAndTranslation: KeyAndTranslation) {
            sectionKeyTv.text = """${keyAndTranslation.key} | ${keyAndTranslation.translation}"""
            itemView.setOnClickListener {
                callback?.invoke(keyAndTranslation)
            }
        }
    }
}
