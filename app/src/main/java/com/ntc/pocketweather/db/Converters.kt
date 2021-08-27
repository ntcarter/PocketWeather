package com.ntc.pocketweather.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.ntc.pocketweather.api.response.*

private const val TAG = "Converters"

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCurrent(current: Current): String {
        return gson.toJson(current).toString()
    }

    @TypeConverter
    fun toCurrent(str: String): Current {
        val json: JsonObject = JsonParser().parse(str).asJsonObject

        val myType = object : TypeToken<List<Weather>>() {}.type
        val weather = gson.fromJson<List<Weather>>(json["weather"], myType)

        return Current(
            json["clouds"].asInt,
            json["dew_point"].asDouble,
            json["dt"].asInt,
            json["feels_like"].asDouble,
            json["humidity"].asInt,
            json["pressure"].asInt,
            json["sunrise"].asInt,
            json["sunset"].asInt,
            json["temp"].asDouble,
            json["uvi"].asDouble,
            json["visibility"].asInt,
            weather,
            json["wind_deg"].asInt,
            json["wind_speed"].asDouble
        )
    }

    @TypeConverter
    fun fromListDaily(list: List<Daily>): String {
        return gson.toJson(list).toString()
    }

    @TypeConverter
    fun toListDaily(str: String): List<Daily> {
        val dailyList: MutableList<Daily> = mutableListOf()
        val jsonArr = JsonParser().parse(str).asJsonArray

        // for each index in json create a weatherX object and add it to a list to return
        val myType = object : TypeToken<List<WeatherX>>() {}.type

        for (daily in jsonArr) {
            val dailyJson = daily.asJsonObject

            //Get the WeatherX list
            val weatherX = gson.fromJson<List<WeatherX>>(dailyJson["weather"], myType)

            // Create the feelsLike Object
            val feelsLikeJson = dailyJson["feels_like"].asJsonObject
            val feelsLike = FeelsLike(
                feelsLikeJson["day"].asDouble,
                feelsLikeJson["eve"].asDouble,
                feelsLikeJson["morn"].asDouble,
                feelsLikeJson["night"].asDouble
            )

            // Create the Temp (temperature) object
            val tempJson = dailyJson["temp"].asJsonObject
            val temp = Temp(
                tempJson["day"].asDouble,
                tempJson["eve"].asDouble,
                tempJson["max"].asDouble,
                tempJson["min"].asDouble,
                tempJson["morn"].asDouble,
                tempJson["night"].asDouble
            )

            dailyList.add(
                Daily(
                    dailyJson["clouds"].asInt,
                    dailyJson["dew_point"].asDouble,
                    dailyJson["dt"].asInt,
                    feelsLike,
                    dailyJson["humidity"].asInt,
                    dailyJson["moon_phase"].asDouble,
                    dailyJson["moonrise"].asInt,
                    dailyJson["moonSet"].asInt,
                    dailyJson["pop"].asDouble,
                    dailyJson["pressure"].asInt,
                    dailyJson["rain"].asDouble,
                    dailyJson["sunrise"].asInt,
                    dailyJson["sunset"].asInt,
                    temp,
                    dailyJson["uvi"].asDouble,
                    weatherX,
                    dailyJson["wind_deg"].asInt,
                    dailyJson["wind_gust"].asDouble,
                    dailyJson["wind_speed"].asDouble
                )
            )
        }
        return dailyList
    }

    @TypeConverter
    fun fromListHourly(list: List<Hourly>): String {
        return gson.toJson(list).toString()
    }

    @TypeConverter
    fun toListHourly(str: String): List<Hourly> {
        val hourlyList: MutableList<Hourly> = mutableListOf()
        val jsonArr = JsonParser().parse(str).asJsonArray

        // for each index in json create a weatherXX object and add it to the list to return
        val myType = object : TypeToken<List<WeatherXX>>() {}.type
        for (hourly in jsonArr) {
            val hourlyJson = hourly.asJsonObject
            //Get the WeatherXX list
            var weatherXX = gson.fromJson<List<WeatherXX>>(hourlyJson["weather"], myType)
            if(weatherXX == null){
                weatherXX = listOf()
            }

            //Create the Rain Object
            val rain = if (hourlyJson["rain"] == null) {
                Rain(0.0)
            } else {
                val rainJson = hourlyJson["rain"].asJsonObject
                Rain(
                    rainJson["oneHour"].asDouble
                )
            }

            hourlyList.add(
                Hourly(
                    hourlyJson["clouds"].asInt,
                    hourlyJson["dew_point"].asDouble,
                    hourlyJson["dt"].asInt,
                    hourlyJson["feels_like"].asDouble,
                    hourlyJson["humidity"].asInt,
                    hourlyJson["pop"].asDouble,
                    hourlyJson["pressure"].asInt,
                    rain,
                    hourlyJson["temp"].asDouble,
                    hourlyJson["uvi"].asDouble,
                    hourlyJson["visibility"].asInt,
                    weatherXX,
                    hourlyJson["wind_deg"].asInt,
                    hourlyJson["wind_gust"].asDouble,
                    hourlyJson["wind_speed"].asDouble
                )
            )
        }
        return hourlyList
    }

    @TypeConverter
    fun fromListMinutely(list: List<Minutely>): String {
        return gson.toJson(list).toString()
    }

    @TypeConverter
    fun toListMinutely(str: String): List<Minutely> {
        val minutelyList: MutableList<Minutely> = mutableListOf()
        val jsonArr = JsonParser().parse(str).asJsonArray

        for (minutely in jsonArr) {
            val minutelyJson = minutely.asJsonObject

            minutelyList.add(
                Minutely(
                    minutelyJson["dt"].asInt,
                    minutelyJson["precipitation"].asDouble
                )
            )
        }
        return minutelyList
    }

    @TypeConverter
    fun fromListAlert(list: List<Alert>): String {
        return gson.toJson(list).toString()
    }

    @TypeConverter
    fun toListAlert(str: String): List<Alert> {
        val alertList: MutableList<Alert> = mutableListOf()
        val jsonArr = JsonParser().parse(str).asJsonArray

        for(alert in jsonArr) {
            val alertJson = alert.asJsonObject

            alertList.add(
                Alert(
                    alertJson["description"].asString,
                    alertJson["end"].asInt,
                    alertJson["event"].asString,
                    alertJson["sender_name"].asString,
                    alertJson["start"].asInt,
                    listOf() // dont save tags
                )
            )
        }
        return alertList
    }
}