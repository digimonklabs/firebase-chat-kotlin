package com.firebase.chat.base

import android.app.Application
import com.firebase.chat.koin.repositoryModule
import com.firebase.chat.koin.restClientModule
import com.firebase.chat.koin.useCaseModule
import com.firebase.chat.koin.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ChatApplication)
            modules(listOf(viewModelModule, repositoryModule, useCaseModule, restClientModule))
        }
    }
}