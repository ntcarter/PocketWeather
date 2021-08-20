package com.ntc.pocketweather.forecast

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntc.pocketweather.api.response.Forecast
import com.ntc.pocketweather.repository.ForecastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ForecastViewModel"

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val repository: ForecastRepository
) : ViewModel() {

    var curForecast: Forecast? = null

    private var latitude: Double = 40.770922 // Default location
    private var longitude: Double = -73.974239 // Default location

    fun getForecastFromDB() = repository.getForecastFromDB()

    fun getNewForecast() {
        Log.d(TAG, "getNewForecast: GETTING NEW FORECAST")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newForecast = repository.getForecastFromApi(latitude, longitude)
                Log.d(TAG, "getNewForecast: FORECAST: $newForecast")
                Log.d(TAG, "getNewForecast: HOURLY: ${newForecast.hourly}")

                curForecast = getForecastFromDB().value // get the current Forecast from the DB

                if (curForecast != null){
                    // remove the current forecast from the DB
                    Log.d(TAG, "getNewForecast: REMOVING CURRENT FORECAST $curForecast")
                    repository.delete(curForecast!!)
                }
                Log.d(TAG, "getNewForecast: INSERTINGGGGGGGGGGGGGG")
                repository.upsert(newForecast) // insert the new forecast
            } catch (e: Exception) {
                Log.d(TAG, "requestForecast: ERROR ${e.message}")
            }
        }
    }

    fun setNewLocation(lat: Double, lon: Double) {
        latitude = lat
        longitude = lon
    }
}