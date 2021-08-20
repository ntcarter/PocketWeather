package com.ntc.pocketweather.api.response

data class Minutely(
    val dt: Int,
    val precipitation: Int
) : WeatherMarker