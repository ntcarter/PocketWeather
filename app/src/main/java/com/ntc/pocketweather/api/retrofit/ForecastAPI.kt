package com.ntc.pocketweather.api.retrofit

import com.ntc.pocketweather.api.response.Forecast
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastAPI {

    companion object {
        const val API_KEY = "3746dbdd4b2522fc1615e228ea067877"
        const val BASE_URL = "https://api.openweathermap.org"
    }

    @GET("/data/2.5/onecall")
    suspend fun getForecastData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "imperial",
        @Query("appid") apiKey: String = API_KEY
    ): Forecast
}