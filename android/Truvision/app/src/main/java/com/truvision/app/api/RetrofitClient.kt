package com.truvision.app.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private var currentBaseUrl: String? = null
    private var apiInstance: TruVisionApi? = null
    
    fun getApi(baseUrl: String): TruVisionApi {
        if (apiInstance == null || currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            apiInstance = createApi(baseUrl)
        }
        return apiInstance!!
    }
    
    private fun createApi(baseUrl: String): TruVisionApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        
        return retrofit.create(TruVisionApi::class.java)
    }
}
