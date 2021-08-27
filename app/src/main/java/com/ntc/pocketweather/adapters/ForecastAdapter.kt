package com.ntc.pocketweather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ntc.pocketweather.R
import com.ntc.pocketweather.api.response.Daily
import com.ntc.pocketweather.api.response.Hourly
import com.ntc.pocketweather.api.response.Minutely
import com.ntc.pocketweather.api.response.WeatherMarker
import com.ntc.pocketweather.databinding.ItemWeatherBinding
import com.ntc.pocketweather.util.convertTime
import com.ntc.pocketweather.util.convertTimeToDay
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
        when (item.javaClass.toString()) {
            "class com.ntc.pocketweather.api.response.Minutely" -> {
                holder.bindMinutely(items[position] as Minutely)
            }
            "class com.ntc.pocketweather.api.response.Hourly" -> {
                holder.bindHourly(items[position] as Hourly)
            }
            "class com.ntc.pocketweather.api.response.Daily" -> {
                holder.bindDaily(items[position] as Daily)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnWeatherClickListener {
        fun onDailyClick(daily: Daily)
    }

    inner class ForecastViewHolder(private val binding: ItemWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindMinutely(minutely: Minutely) {
            binding.apply {
                tvWeatherItemTitle.text = convertTime(minutely.dt, false)
                tvItemWeatherCondition.text = "${minutely.precipitation} mm"
                ivWeatherItemIcon.setImageResource(
                    when (minutely.precipitation) {
                        in 0.0.. 0.1 -> R.drawable.few_clouds_new
                        in 0.1 .. 3.0 -> R.drawable.rain_new
                        in 3.0.. 100.0 -> R.drawable.shower_rain_new
                        else -> R.drawable.few_clouds_new
                    }
                )
            }
        }

        fun bindHourly(hourly: Hourly) {
            binding.apply {
                tvWeatherItemTitle.text = convertTime(hourly.dt, false)
                tvItemWeatherCondition.text = hourly.temp.toString() + "°"
                if(hourly.weather.isNotEmpty()){
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
            binding.root.setOnClickListener {
                listener.onDailyClick(daily)
            }
            binding.apply {
                tvWeatherItemTitle.text = convertTimeToDay(daily.dt)
                tvItemWeatherCondition.text = daily.temp.max.toString() + "°"
                ivWeatherItemIcon.setImageResource(
                    getWeatherIcon(daily.weather[0].icon)
                )
            }
        }
    }
}