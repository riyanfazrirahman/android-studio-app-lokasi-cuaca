package com.dicoding.applokasicuaca.data.retrofitimport com.dicoding.applokasicuaca.BuildConfigimport retrofit2.Retrofitimport retrofit2.converter.gson.GsonConverterFactoryobject WaetherApiConfig {    fun getApiService(): WeatherApiService {        return getRetrofit().create(WeatherApiService::class.java)    }    private fun getRetrofit(): Retrofit {        return Retrofit.Builder()            .baseUrl(BuildConfig.BASE_URL)            .addConverterFactory(GsonConverterFactory.create())            .build()    }}