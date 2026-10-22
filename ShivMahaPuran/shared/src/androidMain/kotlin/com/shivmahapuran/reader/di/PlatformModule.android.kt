package com.shivmahapuran.reader.di

import android.content.Context
import com.shivmahapuran.reader.data.local.DatabaseDriverFactory
import org.koin.dsl.module

fun androidPlatformModule(context: Context) = module {
    single { DatabaseDriverFactory(context.applicationContext) }
}
