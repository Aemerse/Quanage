package com.aemerse.quanage.models

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatCheckBox
import com.aemerse.quanage.R

class RNGSettingsViewHolder(view: View, context: Context, rngSettings: RNGSettings) {
    var sortOptions: Spinner = view.findViewById(R.id.sort_options)
    var blockDupes: AppCompatCheckBox = view.findViewById(R.id.duplicates_toggle)
    var showSum: AppCompatCheckBox = view.findViewById(R.id.show_sum)
    var hideExcludes: AppCompatCheckBox = view.findViewById(R.id.hide_excludes)
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
        sortOptions.setSelection(rngSettings.sortType)
        blockDupes.isChecked = rngSettings.isNoDupes
        showSum.isChecked = rngSettings.isShowSum
        hideExcludes.isChecked = rngSettings.isHideExcluded
    }
}