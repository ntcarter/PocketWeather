package com.ntc.pocketweather.api.geocoding

data class CityItem(
    val Bearing: Double,
    val City: String,
    val CompassDirection: String,
    val Country: String,
    val CountryId: String,
    val Distance: Double,
    val Latitude: Double,
    val LocalTimeNow: String,
    val Longitude: Double,
    val Population: Int,
    val TimeZoneId: String,
    val TimeZoneName: String,
    val TimeZone_GMT_offset: Int
)