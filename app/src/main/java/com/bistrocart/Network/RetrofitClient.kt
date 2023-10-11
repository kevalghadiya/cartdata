package com.Network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.bistrocart.BuildConfig
import com.bistrocart.BuildConfig.BASE_URL
import com.retailer.Network.ApiService
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {

    companion object {

        fun getRetrofitInstance(okHttpClient: OkHttpClient): Retrofit {
            val gson: Gson = GsonBuilder().setLenient().create()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build()
        }


        fun getApiService(okHttpClient: OkHttpClient): ApiService {
            return getRetrofitInstance(okHttpClient).create(ApiService::class.java)
        }

    }


}