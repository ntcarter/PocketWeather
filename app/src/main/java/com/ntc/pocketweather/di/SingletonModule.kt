package com.ntc.pocketweather.di

import android.content.Context
import androidx.room.Room
import com.ntc.pocketweather.api.retrofit.ForecastAPI
import com.ntc.pocketweather.api.retrofit.ForecastAPI.Companion.BASE_URL
import com.ntc.pocketweather.db.WeatherDatabase
import com.ntc.pocketweather.repository.ForecastRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @Singleton
    fun provideForecastDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        WeatherDatabase::class.java,
        "pocket_weather_db.db"
    ).build()

    @Provides
    @Singleton
    fun provideForecastRepo(
        db: WeatherDatabase,
        api: ForecastAPI
    ) = ForecastRepository(db, api)

    @Provides
    @Singleton
    fun providesRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideForecastApi(retrofit: Retrofit): ForecastAPI =
        retrofit.create(ForecastAPI::class.java)
}