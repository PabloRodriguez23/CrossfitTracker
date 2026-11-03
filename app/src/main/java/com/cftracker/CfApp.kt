package com.cftracker

import android.app.Application
import com.cftracker.data.AppDatabase
import com.cftracker.data.TrackerRepository

class CfApp : Application() {
    val repository: TrackerRepository by lazy { TrackerRepository(AppDatabase.build(this).dao()) }
}
