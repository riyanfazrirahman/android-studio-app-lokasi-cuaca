package com.dicoding.applokasicuaca.dataimport com.dicoding.applokasicuaca.data.response.WeatherResponseimport com.dicoding.applokasicuaca.data.retrofit.WeatherApiServiceclass WeatherRepository(private val weatherApiService: WeatherApiService) {    suspend fun getWeatherByCoordinates(lat: Double, lon:Double, apiKey: String): WeatherResponse {        return weatherApiService.getWeatherByCoordinates(lat, lon, apiKey)    }}