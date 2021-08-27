package com.ntc.pocketweather.api.retrofit

import com.ntc.pocketweather.BuildConfig
import com.ntc.pocketweather.api.geocoding.City
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeocodingAPI {
    companion object {
        const val GEOCODING_BASE_URL = "https://geocodeapi.p.rapidapi.com"
    }

    @Headers("x-rapidapi-host: geocodeapi.p.rapidapi.com", "x-rapidapi-key: ${BuildConfig.GEOCODING_API_KEY}")
    @GET("/GetNearestCities")
    suspend fun getGeocodingData(
        @Query("range") range: Int,
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): City
}