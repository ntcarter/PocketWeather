package com.ntc.pocketweather.forecast

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ntc.pocketweather.api.geocoding.City
import com.ntc.pocketweather.api.response.Alert
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

    private var latitude: Double = 40.770922 // Default location
    private var longitude: Double = -73.974239 // Default location

    fun getForecastFromDB() = repository.getForecastFromDB()

    // alerts live data
    private val _alerts = MutableLiveData<List<Alert>>()
    val alerts: LiveData<List<Alert>>
        get() = _alerts

    fun getNewForecast() {
        Log.d(TAG, "getNewForecast: GETTING NEW FORECAST")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newForecast = repository.getForecastFromApi(latitude, longitude)

                Log.d(TAG, "getNewForecast: FORECAST $newForecast")

                if(newForecast.alerts.isNullOrEmpty()) {
                    newForecast.alerts = listOf() // no alerts so it will be null. Save empty list instead
                }

               updateAlerts(newForecast)// alerts will either be a list of alerts or an empty list by this point


                val count = repository.getCount()

                if (count >= 1){
                    // remove the current forecast from the DB
                    repository.deleteAllForecasts()
                }

                getCityFromGeolocation(newForecast)
            } catch (e: Exception) {
                // error do nothing
                Log.d(TAG, "requestForecast: ERROR ${e.message}")
            }
        }
    }

    fun setNewLocation(lat: Double, lon: Double) {
        latitude = lat
        longitude = lon
    }

    fun updateAlerts(forecast: Forecast){
        if(!forecast.alerts.isNullOrEmpty()){
            _alerts.postValue(forecast.alerts!!)
        }else {
            _alerts.postValue(listOf())
        }
    }

    private fun getCityFromGeolocation(newForecast: Forecast){
        var city: City?

        viewModelScope.launch(Dispatchers.IO) {
            try {
                city = repository.getGeocodingFromApi(latitude, longitude)
                newForecast.CityName = city!![0].City

                repository.upsert(newForecast) // insert the new forecast
            }catch (e: Exception){
                Log.d(TAG, "getCityFromGeolocation: ERROR ${e.message}")
            }
        }
    }
}