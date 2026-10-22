package com.shivmahapuran.reader.android

import android.app.Application
import com.shivmahapuran.reader.di.androidPlatformModule
import com.shivmahapuran.reader.di.initKoin
import com.shivmahapuran.reader.platform.AndroidPlatform

class PuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatform.init(this)
        initKoin(androidPlatformModule(this))
    }
}
