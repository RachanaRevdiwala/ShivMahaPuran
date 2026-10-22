package com.shivmahapuran.reader.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.shivmahapuran.reader.db.PuranDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver = AndroidSqliteDriver(PuranDatabase.Schema, context, DATABASE_NAME)
}
