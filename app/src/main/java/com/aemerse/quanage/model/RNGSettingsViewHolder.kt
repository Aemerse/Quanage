package com.aemerse.quanage.model

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatCheckBox
import com.aemerse.quanage.R

class RNGSettingsViewHolder(view: View, context: Context, QRNGSettings: QRNGSettings) {
    private var sortOptions: Spinner = view.findViewById(R.id.sort_options)
    private var blockDupes: AppCompatCheckBox = view.findViewById(R.id.duplicates_toggle)
    private var showSum: AppCompatCheckBox = view.findViewById(R.id.show_sum)
    private var hideExcludes: AppCompatCheckBox = view.findViewById(R.id.hide_excludes)
    val noDupes: Boolean get() = blockDupes.isChecked
    val sortIndex: Int get() = sortOptions.selectedItemPosition

    fun showSum(): Boolean {
        return showSum.isChecked
    }

    fun hideExcludes(): Boolean {
        return hideExcludes.isChecked
    }

    init {
        val sortChoices = context.resources.getStringArray(R.array.sort_options)
        sortOptions.adapter = ArrayAdapter(context, R.layout.spinner_item_rng_settings, sortChoices)
        sortOptions.setSelection(QRNGSettings.sortType)
        blockDupes.isChecked = QRNGSettings.isNoDupes
        showSum.isChecked = QRNGSettings.isShowSum
        hideExcludes.isChecked = QRNGSettings.isHideExcluded
    }
}