package com.ntc.pocketweather.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ntc.pocketweather.api.response.Forecast

@Database(
    entities = [Forecast::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun getArticleDao(): WeatherDao

    companion object {
        @Volatile
        private var instance: WeatherDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                WeatherDatabase::class.java,
                "pocket_weather_db.db"
            ).build()
    }
}