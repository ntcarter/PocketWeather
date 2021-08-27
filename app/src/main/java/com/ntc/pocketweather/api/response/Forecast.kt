package com.ntc.pocketweather.api.response

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "forecast",
)
data class Forecast(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    var alerts: List<Alert>?,
    val current: Current,
    val daily: List<Daily>,
    val hourly: List<Hourly>,
    val lat: Double,
    val lon: Double,
    val minutely: List<Minutely>,
    val timezone: String,
    val timezone_offset: Int,
    var CityName: String = ""
)