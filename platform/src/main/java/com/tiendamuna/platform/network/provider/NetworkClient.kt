package com.tiendamuna.platform.network.provider

import android.content.Context
import com.tiendamuna.platform.network.interceptors.LoggingInterceptorFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkClient private constructor(
    val retrofit: Retrofit,
    val okHttpClient: OkHttpClient
) {

    /**
     * Helper para crear servicios de Retrofit de forma concisa.
     * Ejemplo: client.createService(UserApiService::class.java)
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
    class Builder(private val baseUrl: String) {
        private var connectTimeout = 15L
        private var readTimeout = 30L
        private var writeTimeout = 30L
        private var isDebug = false
        private var tokenProvider: (() -> String?)? = null
        private var cacheContext: Context? = null
        private var cacheSizeBytes = 10L * 1024 * 1024
        private val interceptors = mutableListOf<Interceptor>()

        fun setConnectTimeout(seconds: Long) = apply { this.connectTimeout = seconds }
        fun setReadTimeout(seconds: Long) = apply { this.readTimeout = seconds }
        fun setWriteTimeout(seconds: Long) = apply { this.writeTimeout = seconds }
        fun setDebug(isDebug: Boolean) = apply { this.isDebug = isDebug }
        fun setTokenProvider(provider: () -> String?) = apply { this.tokenProvider = provider }

        fun enableCache(context: Context, sizeInBytes: Long = 10L * 1024 * 1024) = apply {
            this.cacheContext = context.applicationContext
            this.cacheSizeBytes = sizeInBytes
        }

        fun addInterceptor(interceptor: Interceptor) = apply {
            this.interceptors.add(interceptor)
        }

        fun build(): NetworkClient {
            val okHttpBuilder = OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)

            // Configurar Caché opcional
            cacheContext?.let { ctx ->
                val cacheDir = File(ctx.cacheDir, "platform_http_cache")
                okHttpBuilder.cache(Cache(cacheDir, cacheSizeBytes))
            }

            // Interceptores personalizados
            interceptors.forEach { okHttpBuilder.addInterceptor(it) }

            okHttpBuilder.addInterceptor(
                LoggingInterceptorFactory.create(isDebug))

            val client = okHttpBuilder.build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return NetworkClient(retrofit, client)
        }
    }
}