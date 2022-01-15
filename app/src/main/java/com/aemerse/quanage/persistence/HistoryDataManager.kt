package com.aemerse.quanage.persistence

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.SparseArray
import com.aemerse.quanage.constants.RNGType
import java.util.*

class HistoryDataManager private constructor(context: Context) {
    interface Listener {
        fun onInitialHistoryDataFetched(historyRecords: SparseArray<List<CharSequence>>?)
        fun onHistoryRecordAdded(@RNGType rngType: Int, recordText: String?)
    }

    private val dataSource: HistoryDataSource? = HistoryDataSource.get(context)
    private val listeners: MutableSet<Listener>
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun addHistoryRecord(@RNGType rngType: Int, recordText: String?) {
        dataSource!!.addHistoryRecord(rngType, recordText)
        for (listener in listeners) {
            listener.onHistoryRecordAdded(rngType, recordText)
        }
    }

    val initialHistory: Unit
        get() {
            val handlerThread = HandlerThread(UUID.randomUUID().toString())
            handlerThread.start()
            val backgroundHandler = Handler(handlerThread.looper)
            backgroundHandler.post {
                val historyRecords = dataSource?.history
                for (listener in listeners) {
                    listener.onInitialHistoryDataFetched(historyRecords)
                }
            }
        }

    fun deleteHistory(@RNGType rngType: Int) {
        dataSource!!.deleteHistory(rngType)
    }

    companion object {
        private var instance: HistoryDataManager? = null
        @JvmStatic
        operator fun get(context: Context): HistoryDataManager? {
            if (instance == null) {
                instance = getSync(context)
            }
            return instance
        }

        @Synchronized
        private fun getSync(context: Context): HistoryDataManager? {
            if (instance == null) {
                instance = HistoryDataManager(context)
            }
            return instance
        }
    }

    init {
        listeners = HashSet()
    }
}