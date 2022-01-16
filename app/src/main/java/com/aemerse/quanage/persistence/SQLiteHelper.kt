package com.aemerse.quanage.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper internal constructor(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(CREATE_HISTORY_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        // Table name
        const val TABLE_NAME = "history"

        // COLUMNS
        const val COLUMN_RNG_TYPE = "rng_type"
        const val COLUMN_RECORD_TEXT = "record_text"
        const val COLUMN_TIME_INSERTED = "time_inserted"

        // Some random things fed to a super's method
        private const val DATABASE_NAME = "RandomNumberGeneratorPlus.db"
        private const val DATABASE_VERSION = 1

        // Database creation sql statements
        private const val CREATE_HISTORY_TABLE = ("CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME + "(" + COLUMN_RNG_TYPE
                + " INTEGER, " + COLUMN_RECORD_TEXT + " TEXT, " + COLUMN_TIME_INSERTED + " INTEGER);")
    }
}