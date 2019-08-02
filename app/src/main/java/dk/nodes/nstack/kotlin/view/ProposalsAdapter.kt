package dk.nodes.nstack.kotlin.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.nodes.nstack.R
import dk.nodes.nstack.models.Proposal

class ProposalsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            Header -> HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false))
            Row -> RowViewHolder(inflater.inflate(R.layout.item_proposal, parent, false))

            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RowViewHolder -> holder.bind(list[position] as Item.Row)
            is HeaderViewHolder -> holder.bind(list[position] as Item.Header)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(list[position]) {
            is Item.Row -> Row
            else -> Header
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun update(newList: List<Proposal>) {
        list.clear()
        newList.groupBy { it.key }.forEach { entry ->
            list += Item.Header(entry.key)
            entry.value.forEach {
                list += Item.Row(it.id, it.value)
            }
        }
        notifyDataSetChanged()
    }

    private class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val proposalTv: TextView = itemView.findViewById(R.id.proposalTv)

        fun bind(row: Item.Row) {
            proposalTv.text = row.value
        }
    }

    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTv: TextView = itemView.findViewById(R.id.headerTv)
        fun bind(header: Item.Header) {
            headerTv.text = header.key
        }
    }

    private sealed class Item {
        data class Header(val key: String) : Item()
        data class Row(val id: Long, val value: String) : Item()
    }

    companion object {
        private const val Header = 0
        private const val Row = 1
    }
}