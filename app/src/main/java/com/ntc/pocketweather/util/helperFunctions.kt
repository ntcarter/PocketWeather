package com.ntc.pocketweather.util

import com.ntc.pocketweather.R

fun getWeatherIcon(iconId: String): Int {
    return when (iconId) {
        "01d", "01n" -> R.drawable.clear_sky_new
        "02d", "02n" -> R.drawable.few_clouds_new
        "03d", "03n" -> R.drawable.scattered_clouds_new
        "04d", "04n" -> R.drawable.broken_clouds_new
        "09d", "09n" -> R.drawable.shower_rain_new
        "10d", "10n" -> R.drawable.rain_new
        "11d", "11n" -> R.drawable.thunderstorm_new
        "13d", "13n" -> R.drawable.snow_new
        "50d", "50n" -> R.drawable.mist_new
        else -> R.drawable.scattered_clouds_new
    }
}