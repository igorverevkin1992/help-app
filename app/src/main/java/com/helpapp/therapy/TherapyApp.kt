package com.helpapp.therapy

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.helpapp.therapy.security.SessionLock
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TherapyApp : Application() {

    @Inject lateinit var sessionLock: SessionLock

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                sessionLock.evaluateOnForeground()
            }

            override fun onStop(owner: LifecycleOwner) {
                sessionLock.markBackgrounded()
            }
        })
    }
}
