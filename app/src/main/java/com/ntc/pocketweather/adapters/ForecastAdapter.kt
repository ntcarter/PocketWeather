package com.ntc.pocketweather.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ntc.pocketweather.R
import com.ntc.pocketweather.api.response.Daily
import com.ntc.pocketweather.api.response.Hourly
import com.ntc.pocketweather.api.response.Minutely
import com.ntc.pocketweather.api.response.WeatherMarker
import com.ntc.pocketweather.databinding.ItemWeatherBinding
import com.ntc.pocketweather.util.getWeatherIcon

private const val TAG = "ForecastAdapter"

class ForecastAdapter(
    var items: List<WeatherMarker>,
    private val listener: OnWeatherClickListener
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = items[position]
        Log.d(TAG, "onBindViewHolder: item class : ${item.javaClass}")
        when (item.javaClass.toString()) {
            "class com.ntc.pocketweather.api.response.Minutely" -> {
                Log.d(TAG, "onBindViewHolder: BINDING MINUTELY")
                holder.bindMinutely(items[position] as Minutely)
            }
            "class com.ntc.pocketweather.api.response.Hourly" -> {
                Log.d(TAG, "onBindViewHolder:  BINDING Hourly")
                holder.bindHourly(items[position] as Hourly)
            }
            "class com.ntc.pocketweather.api.response.Daily" -> {
                Log.d(TAG, "onBindViewHolder:  BINDING Daily ")
                holder.bindDaily(items[position] as Daily)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnWeatherClickListener {

    }

    inner class ForecastViewHolder(private val binding: ItemWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindMinutely(minutely: Minutely) {
            binding.apply {
                tvWeatherItemTitle.text = minutely.dt.toString()
                tvItemWeatherCondition.text = "${minutely.precipitation.toString()}%"
                ivWeatherItemIcon.setImageResource(
                    when (minutely.precipitation) {
                        in 0..15 -> R.drawable.few_clouds_new
                        in 16..50 -> R.drawable.rain_new
                        in 50..75 -> R.drawable.shower_rain_new
                        in 75..100 -> R.drawable.thunderstorm_new
                        else -> R.drawable.few_clouds_new
                    }
                )
            }
        }

        fun bindHourly(hourly: Hourly) {
            binding.apply {
                tvWeatherItemTitle.text = hourly.dt.toString()
                tvItemWeatherCondition.text = hourly.temp.toString()
                if(hourly.weather.isNotEmpty()){
                    val tmp = hourly.weather[0]
                    ivWeatherItemIcon.setImageResource(
                        getWeatherIcon(hourly.weather[0].icon)
                    )
                } else {
                    ivWeatherItemIcon.setImageResource(
                        R.drawable.few_clouds_new
                    )
                }
            }
        }

        fun bindDaily(daily: Daily) {
            binding.apply {
                tvWeatherItemTitle.text = daily.dt.toString()
                tvItemWeatherCondition.text = daily.temp.toString()
                ivWeatherItemIcon.setImageResource(
                    getWeatherIcon(daily.weather[0].icon)
                )
            }
        }
    }
}