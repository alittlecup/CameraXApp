package com.hbl.cameraxapp

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: MyApplication
    }
}