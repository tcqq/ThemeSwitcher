package com.example.themeswitcher.app


import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.example.themeswitcher.BuildConfig
import timber.log.Timber

/**
 * @author Alan Dreamer
 * @since 22/10/2016 Created
 */
class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
