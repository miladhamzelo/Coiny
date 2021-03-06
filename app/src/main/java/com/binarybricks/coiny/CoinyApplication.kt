package com.binarybricks.coiny

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.support.annotation.NonNull
import android.util.Log
import com.binarybricks.coiny.data.database.CoinyDatabase
import com.facebook.stetho.Stetho
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
Created by Pranay Airan 1/8/18.
 */

class CoinyApplication : Application() {

    private val DATABASE_NAME = "coiny.db"

    companion object {
        private lateinit var appContext: Context

        var database: CoinyDatabase? = null

        @JvmStatic
        fun getGlobalAppContext(): Context {
            return appContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Stetho.initializeWithDefaults(this)
        } else {
            Timber.plant(CrashReportingTree())
        }

        database = Room.databaseBuilder(this, CoinyDatabase::class.java, DATABASE_NAME).build()
    }

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String, @NonNull message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            //TODO remove or enable this.
//            FakeCrashLibrary.log(priority, tag, message)
//
//            if (t != null) {
//                if (priority == Log.ERROR) {
//                    FakeCrashLibrary.logError(t)
//                } else if (priority == Log.WARN) {
//                    FakeCrashLibrary.logWarning(t)
//                }
//            }
        }
    }
}