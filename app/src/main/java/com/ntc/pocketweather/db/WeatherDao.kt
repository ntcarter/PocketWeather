package com.ntc.pocketweather.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ntc.pocketweather.api.response.Forecast

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(Forecast: Forecast)

    @Delete
    suspend fun delete(Forecast: Forecast)

    @Query("SELECT * FROM forecast ORDER BY _id ASC LIMIT 1")
    fun getCurrentForecast(): LiveData<Forecast>
}