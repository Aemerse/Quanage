package com.aemerse.quanage.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.aemerse.quanage.R
import com.aemerse.quanage.adapters.HistoryAdapter.HistoryItemViewHolder
import com.aemerse.quanage.utils.copyHistoryRecordToClipboard
import com.aemerse.quanage.utils.stripHtml
import java.util.*

class HistoryAdapter : RecyclerView.Adapter<HistoryItemViewHolder>() {
    var items: MutableList<CharSequence> = ArrayList()
    fun setInitialItems(initialItems: List<CharSequence>?) {
        val uiHandler = Handler(Looper.getMainLooper())
        uiHandler.post {
            items.addAll(initialItems!!)
            notifyDataSetChanged()
        }
    }

    fun addItem(item: CharSequence) {
        items.add(0, item)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.history_list_item,
                parent,
                false)
        return HistoryItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.loadItem(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class HistoryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemText: TextView = view.findViewById(R.id.item_text)
        fun loadItem(position: Int) {
            itemText.text = HtmlCompat.fromHtml(items[position].toString(),HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        init {
            itemText.setOnClickListener { view1: View ->
                val text = items[adapterPosition]
                copyHistoryRecordToClipboard(
                        stripHtml(text.toString()),
                        view1.context)
            }
        }
    }
}