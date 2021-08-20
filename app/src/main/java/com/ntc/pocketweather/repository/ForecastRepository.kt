package com.ntc.pocketweather.repository

import com.ntc.pocketweather.api.response.Forecast
import com.ntc.pocketweather.api.retrofit.ForecastAPI
import com.ntc.pocketweather.db.WeatherDatabase
import javax.inject.Inject

class ForecastRepository @Inject constructor(
    private val db: WeatherDatabase,
    private val api: ForecastAPI
) {

    // Retrofit calls
    suspend fun getForecastFromApi(lat: Double, lon: Double) =
        api.getForecastData(lat, lon)

    // DB operations
    suspend fun upsert(forecast: Forecast) = db.getArticleDao().upsert(forecast)

    fun getForecastFromDB() = db.getArticleDao().getCurrentForecast()

    suspend fun delete(forecast: Forecast) = db.getArticleDao().delete(forecast)
}