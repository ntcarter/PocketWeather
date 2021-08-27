package com.ntc.pocketweather.repository

import com.ntc.pocketweather.api.response.Forecast
import com.ntc.pocketweather.api.retrofit.ForecastAPI
import com.ntc.pocketweather.api.retrofit.GeocodingAPI
import com.ntc.pocketweather.db.WeatherDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ForecastRepository @Inject constructor(
    private val db: WeatherDatabase,
    private val forecastApi: ForecastAPI,
    private val geocodingApi: GeocodingAPI
) {

    // Retrofit calls
    suspend fun getForecastFromApi(lat: Double, lon: Double) =
        forecastApi.getForecastData(lat, lon)

    suspend fun getGeocodingFromApi(lat: Double, lon: Double) =
        geocodingApi.getGeocodingData(0 , lat, lon)

    // DB operations
    suspend fun upsert(forecast: Forecast) = db.getArticleDao().upsert(forecast)

    fun getForecastFromDB() = db.getArticleDao().getCurrentForecast()

    suspend fun delete(forecast: Forecast) = db.getArticleDao().delete(forecast)

    suspend fun deleteAllForecasts() = db.getArticleDao().deleteAllForecasts()

    fun getCount(): Int = runBlocking {
        val count = async {
            db.getArticleDao().getCount()
        }
        count.start()
        count.await()
    }
}