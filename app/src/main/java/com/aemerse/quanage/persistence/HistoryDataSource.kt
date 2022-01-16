package com.aemerse.quanage.persistence

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.HandlerThread
import android.util.SparseArray
import com.aemerse.quanage.constants.QRNGType
import java.util.*

class HistoryDataSource private constructor(context: Context) {
    private var database: SQLiteDatabase? = null
    private val dbHelper: SQLiteHelper = SQLiteHelper(context)
    private val backgroundHandler: Handler

    // Open connection to database
    @Throws(SQLException::class)
    private fun open() {
        database = dbHelper.writableDatabase
    }

    // Terminate connection to database
    private fun close() {
        dbHelper.close()
    }

    val history: SparseArray<List<CharSequence>>
        get() {
            val rngTypeToHistoryList = SparseArray<List<CharSequence>>()
            open()
            val columns = arrayOf(
                    SQLiteHelper.COLUMN_RECORD_TEXT,
                    SQLiteHelper.COLUMN_TIME_INSERTED)
            val selection: String = SQLiteHelper.COLUMN_RNG_TYPE + " = ?"
            val orderBy: String = SQLiteHelper.COLUMN_TIME_INSERTED + " DESC"
            val rngTypes = intArrayOf(QRNGType.NUMBER, QRNGType.DICE, QRNGType.COINS)
            for (rngType in rngTypes) {
                val history: MutableList<CharSequence> = ArrayList()
                val selectionArgs = arrayOf(rngType.toString())
                val cursor = database!!.query(
                        SQLiteHelper.TABLE_NAME,
                        columns,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        orderBy, MAX_RECORDS_PER_TYPE.toString())
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        history.add(cursor.getString(0))
                    }
                    cursor.close()
                }
                rngTypeToHistoryList.append(rngType, history)
            }
            close()
            return rngTypeToHistoryList
        }

    fun addHistoryRecord(@QRNGType rngType: Int, recordText: String?) {
        backgroundHandler.post {
            open()
            val values = ContentValues()
            values.put(SQLiteHelper.COLUMN_RNG_TYPE, rngType)
            values.put(SQLiteHelper.COLUMN_RECORD_TEXT, recordText)
            values.put(SQLiteHelper.COLUMN_TIME_INSERTED, System.currentTimeMillis())
            database!!.insert(SQLiteHelper.TABLE_NAME, null, values)
            close()
        }
    }

    fun deleteHistory(@QRNGType rngType: Int) {
        backgroundHandler.post {
            open()
            val whereArgs = arrayOf(rngType.toString())
            database!!.delete(
                    SQLiteHelper.TABLE_NAME,
                    SQLiteHelper.COLUMN_RNG_TYPE + " = ?",
                    whereArgs)
            close()
        }
    }

    companion object {
        private const val MAX_RECORDS_PER_TYPE = 20
        private var instance: HistoryDataSource? = null
        operator fun get(context: Context): HistoryDataSource? {
            if (instance == null) {
                instance = getSync(context)
            }
            return instance
        }

        @Synchronized
        private fun getSync(context: Context): HistoryDataSource? {
            if (instance == null) {
                instance = HistoryDataSource(context)
            }
            return instance
        }
    }

    init {
        val handlerThread = HandlerThread("Database")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
    }
}