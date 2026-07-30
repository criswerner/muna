package com.tiendamuna.stock

import android.app.Application
import com.tiendamuna.stock.di.AppContainer
import com.tiendamuna.stock.di.NetworkModule

class StockApplication : Application() {
    // Instance of AppContainer that will be used by all the Activities of the app
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        NetworkModule.provideNetworkClient(this)
        container = AppContainer(this)
    }
}
