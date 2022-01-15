package com.aemerse.quanage.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.aemerse.quanage.R
import java.util.*

class ExcludedNumbersAdapter(val excludedNumbers: ArrayList<Int>?, private val noExcludes: View) : BaseAdapter() {
    private fun setNoContent() {
        noExcludes.visibility = if (count == 0) View.VISIBLE else View.GONE
    }

    fun containsNumber(number: Int): Boolean? {
        return excludedNumbers?.contains(number)
    }

    fun addNumber(number: Int) {
        excludedNumbers?.add(number)
        excludedNumbers?.sort()
        notifyDataSetChanged()
        setNoContent()
    }

    private fun removeNumber(position: Int) {
        excludedNumbers?.removeAt(position)
        excludedNumbers?.sort()
        notifyDataSetChanged()
        setNoContent()
    }

    override fun getCount(): Int {
        return excludedNumbers!!.size
    }

    override fun getItem(position: Int): Int? {
        return excludedNumbers?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    internal class ExcludedNumberViewHolder(view: View) {
        var excludedNumber: TextView = view.findViewById(R.id.excluded_number)
        var deleteIcon: View = view.findViewById(R.id.delete_icon)
    }

    override fun getView(position: Int, view: View, parent: ViewGroup): View {
        val holder: ExcludedNumberViewHolder = view.tag as ExcludedNumberViewHolder
        holder.excludedNumber.text = getItem(position).toString()
        holder.deleteIcon.setOnClickListener { removeNumber(position) }
        return view
    }

    init {
        setNoContent()
    }
}