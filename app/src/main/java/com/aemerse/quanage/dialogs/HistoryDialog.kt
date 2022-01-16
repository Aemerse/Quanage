package com.aemerse.quanage.dialogs

import android.content.Context
import android.util.SparseArray
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.aemerse.quanage.R
import com.aemerse.quanage.adapters.HistoryAdapter
import com.aemerse.quanage.constants.QRNGType
import com.aemerse.quanage.persistence.HistoryDataManager
import com.aemerse.quanage.utils.SimpleDividerItemDecoration

class HistoryDialog(context: Context, @field:QRNGType @param:QRNGType private val currentRngType: Int, isDarkModeEnabled: Boolean) {
    private lateinit var dialog: MaterialDialog
    private val historyAdapter: HistoryAdapter = HistoryAdapter()
    private val historyDataManager: HistoryDataManager

    private fun resetDialog(context: Context?, isDarkModeEnabled: Boolean) {
        dialog = MaterialDialog.Builder(context!!)
                .theme(if (isDarkModeEnabled) Theme.DARK else Theme.LIGHT)
                .title(titleResource)
                .content(R.string.no_history)
                .adapter(historyAdapter, null)
                .positiveText(R.string.dismiss)
                .onPositive { dialog, which -> dialog.dismiss() }
                .neutralText(R.string.clear)
                .onNeutral { dialog, which ->
                    historyDataManager.deleteHistory(currentRngType)
                    historyAdapter.clear()
                    dialog.setContent(R.string.no_history)
                }
                .autoDismiss(false)
                .build()
        dialog.recyclerView.addItemDecoration(SimpleDividerItemDecoration(context))
        if (historyAdapter.itemCount > 0) {
            dialog.setContent(R.string.history_explanation)
        }
    }

    @get:StringRes
    private val titleResource: Int
        get() = when (currentRngType) {
            QRNGType.NUMBER -> R.string.rng_history
            QRNGType.DICE -> R.string.dice_history
            QRNGType.COINS -> R.string.coins_history
            else -> R.string.rng_history
        }
    private val listener: HistoryDataManager.Listener = object : HistoryDataManager.Listener {
        override fun onInitialHistoryDataFetched(historyRecords: SparseArray<List<CharSequence>>?) {
            val history = historyRecords?.get(currentRngType)
            if (history != null) {
                if (history.isNotEmpty()) {
                    dialog.setContent(R.string.history_explanation)
                }
            }
            historyAdapter.setInitialItems(history)
        }

        override fun onHistoryRecordAdded(@QRNGType rngType: Int, recordText: String?) {
            if (currentRngType == rngType) {
                if (recordText != null) {
                    historyAdapter.addItem(recordText)
                }
                dialog.setContent(R.string.history_explanation)
            }
        }
    }

    fun show() {
        dialog.show()
    }

    init {
        resetDialog(context, isDarkModeEnabled)
        dialog.recyclerView.addItemDecoration(SimpleDividerItemDecoration(context))
        historyDataManager = HistoryDataManager[context]!!
        historyDataManager.addListener(listener)
    }
}