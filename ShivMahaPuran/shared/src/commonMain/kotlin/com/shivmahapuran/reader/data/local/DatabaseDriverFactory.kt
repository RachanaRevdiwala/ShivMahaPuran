package com.shivmahapuran.reader.data.local

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}

internal const val DATABASE_NAME = "shivpuran.db"
