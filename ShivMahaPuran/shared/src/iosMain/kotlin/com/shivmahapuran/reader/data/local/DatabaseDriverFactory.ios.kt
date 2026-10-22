package com.shivmahapuran.reader.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.shivmahapuran.reader.db.PuranDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver = NativeSqliteDriver(PuranDatabase.Schema, DATABASE_NAME)
}
