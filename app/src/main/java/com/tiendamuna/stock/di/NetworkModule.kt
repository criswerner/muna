package com.tiendamuna.stock.di

import android.content.Context
import com.tiendamuna.platform.network.provider.NetworkClient

/**
 * Módulo para proveer la instancia de NetworkClient como Singleton (estático).
 */
object NetworkModule {

    @Volatile
    private var instance: NetworkClient? = null

    fun provideNetworkClient(context: Context): NetworkClient {
        return instance ?: synchronized(this) {
            instance ?: NetworkClient.Builder(baseUrl = "https://api.tu-backend.com/")
                .setConnectTimeout(20)
                .setReadTimeout(30)
                .setDebug(true)
                .enableCache(context)
                .build().also { instance = it }
        }
    }

    /**
     * Crea un servicio de API de forma estática.
     * Requiere que el cliente haya sido inicializado previamente (usualmente en Application).
     */
    fun <T> createService(serviceClass: Class<T>): T {
        val client = instance ?: throw IllegalStateException("NetworkClient no ha sido inicializado. Llama a provideNetworkClient(context) primero.")
        return client.createService(serviceClass)
    }
}
