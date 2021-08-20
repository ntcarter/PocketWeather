package com.ntc.pocketweather.forecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntc.pocketweather.R
import com.ntc.pocketweather.adapters.ForecastAdapter
import com.ntc.pocketweather.api.response.Forecast
import com.ntc.pocketweather.databinding.FragmentForecastBinding
import com.ntc.pocketweather.repository.ForecastRepository
import com.ntc.pocketweather.util.Constants.Companion.PERMISSION_REQUEST_CODE
import com.ntc.pocketweather.util.getWeatherIcon
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


private const val TAG = "ForecastFragment"

@AndroidEntryPoint
class ForecastFragment : Fragment(R.layout.fragment_forecast),
    ForecastAdapter.OnWeatherClickListener {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repository: ForecastRepository

    private val viewModel: ForecastViewModel by viewModels()

    private lateinit var locationManager: LocationManager

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: -----------------------------------")
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentForecastBinding.bind(view)

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val minutelyAdapter = ForecastAdapter(listOf(), this)
        val hourlyAdapter = ForecastAdapter(listOf(), this)
        val dailyAdapter = ForecastAdapter(listOf(), this)

        val dividerLight = DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
        dividerLight.setDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.divider_light
            )!!
        )

        val dividerDark = DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
        dividerDark.setDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.divider_dark
            )!!
        )

        binding.apply {
            rvMinutely.apply {
                setHasFixedSize(true)
                this.adapter = minutelyAdapter
                addItemDecoration(dividerDark)
            }
            rvHourly.apply {
                setHasFixedSize(true)
                this.adapter = hourlyAdapter
                addItemDecoration(dividerLight)
            }
            rvDaily.apply {
                setHasFixedSize(true)
                this.adapter = dailyAdapter
                addItemDecoration(dividerDark)
            }
        }

        viewModel.getForecastFromDB().observe(viewLifecycleOwner, {
            Log.d(TAG, "onViewCreated: llllleleleleLELELELELELELRKKRRKRKRKKRKRKR")
            // when the forecast changes we need to update the ui
            if (it != null) {
                Log.d(TAG, "onViewCreated: UPDATING UI!!!!!!!!!!!!!!!!!!!!")
                updateUI(it)

                minutelyAdapter.items = it.minutely
                minutelyAdapter.notifyDataSetChanged()

                hourlyAdapter.items = it.hourly
                hourlyAdapter.notifyDataSetChanged()

                dailyAdapter.items = it.daily
                dailyAdapter.notifyDataSetChanged()
            }
        })

        binding.ivRefresh.setOnClickListener {
            Toast.makeText(requireContext(), "Loading new weather data", Toast.LENGTH_SHORT).show()
            getNewLocation()
        }

        Log.d(TAG, "onViewCreated: ENDS")
    }

    private fun updateUI(foreCast: Forecast) {

        binding.apply {
            // Text
            tvCurrentDescription.text = foreCast.current.weather[0].description
            tvFeelsLike.text = "Feels like: ${foreCast.current.feels_like}°"
            tvActualTemp.text = "Current Temperature: ${foreCast.current.temp}°"
            tvLat.text = "Latitude: ${foreCast.lat}°"
            tvLon.text = "Longitude: ${foreCast.lon}°"
            tvSunrise.text = "Sunrise: ${convertTime(foreCast.current.sunrise)}"
            tvSunset.text = "Sunset: ${convertTime(foreCast.current.sunset)}"
            tvHumidity.text = "Humidity: ${foreCast.current.humidity}%"
            tvWindSpeed.text = "WindSpeed: ${foreCast.current.wind_speed} m/h"
            tvUVI.text = "UVI: ${foreCast.current.uvi}"
            tvVisibility.text = "Visibility: ${foreCast.current.visibility} meters"

            // Icon
            ivWeatherIcon.setImageResource(getWeatherIcon(foreCast.current.weather[0].icon))
        }
    }


    private fun convertTime(timeInSeconds: Int): String {
        val format = SimpleDateFormat("HH:mm", Locale.US)
        var formattedTime = format.format(Date(timeInSeconds * 1000L))

        val time = formattedTime.substring(0, 2).toInt()

        if (time < 12) {
            formattedTime += " a.m"
        } else {
            if (time > 12) {
                val newTime = "0" + (time - 12).toString()
                formattedTime = formattedTime.replaceRange(0..2, "$newTime:")
            }
            formattedTime += " p.m"
        }
        return formattedTime
    }

    private fun checkPermissions(): Boolean {
        Log.d(TAG, "checkPermissions: CHECKING PERMISSIONS")
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        Log.d(TAG, "isLocationEnabled: CHECKING LOCATION")
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                getLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enable location settings",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.d(TAG, "getNewLocation: PERMISSIONS NOT GRANTED ")
            requestLocationPermissions()
        }
    }

    @SuppressLint("MissingPermission") // this permission is already checked for
    private fun getLocation() {
        Log.d(TAG, "getLocation: CALLED")
        val location: Location? =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            Log.d(
                TAG,
                "onViewCreated: LAT: ${location.latitude} , LONG: ${location.longitude}"
            )
            viewModel.setNewLocation(location.latitude, location.longitude)
            viewModel.getNewForecast()
        }else {
            Log.d(TAG, "getLocation: LOCATION ERROR: $location")
        }
    }

    private fun requestLocationPermissions() {
        Log.d(TAG, "requestLocationPermissions: REQUESTING LOCATION PERMISSIONS")
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_REQUEST_CODE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}