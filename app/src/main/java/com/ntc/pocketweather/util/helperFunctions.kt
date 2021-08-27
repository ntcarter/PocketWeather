package com.ntc.pocketweather.util

import com.ntc.pocketweather.R
import java.text.SimpleDateFormat
import java.util.*

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

fun convertTime(timeInSeconds: Int, includeAMPM: Boolean): String {
    val format = SimpleDateFormat("HH:mm", Locale.US)
    var formattedTime = format.format(Date(timeInSeconds * 1000L))

    val time = formattedTime.substring(0, 2).toInt()

    if (time > 12) {
        val newTime = (time - 12).toString()
        formattedTime = formattedTime.replaceRange(0..2, "$newTime:")
    }

    if (includeAMPM) {
        if (time < 12) {
            formattedTime += " a.m"
        } else {
            formattedTime += " p.m"
        }
    }

    if (formattedTime[0] == '0') {
        formattedTime = formattedTime.substring(1 until formattedTime.length)
    }
    return formattedTime
}

fun convertTimeToDay(timeInSeconds: Int): String {
    val format = SimpleDateFormat("E", Locale.US)
    return format.format(Date(timeInSeconds * 1000L))
}