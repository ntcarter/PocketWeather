package com.ntc.pocketweather.api.retrofit

import com.ntc.pocketweather.BuildConfig
import com.ntc.pocketweather.api.response.Forecast
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastAPI {

    companion object {
        const val FORECAST_BASE_URL = "https://api.openweathermap.org"
    }

    @GET("/data/2.5/onecall")
    suspend fun getForecastData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "imperial",
        @Query("appid") apiKey: String = BuildConfig.FORECAST_API_KEY
    ): Forecast
}