package com.ntc.pocketweather.forecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ntc.pocketweather.R
import com.ntc.pocketweather.adapters.ForecastAdapter
import com.ntc.pocketweather.api.response.Daily
import com.ntc.pocketweather.api.response.Forecast
import com.ntc.pocketweather.databinding.DailyDialogBinding
import com.ntc.pocketweather.databinding.FragmentForecastBinding
import com.ntc.pocketweather.repository.ForecastRepository
import com.ntc.pocketweather.util.Constants.Companion.REQUEST_CHECK_SETTINGS
import com.ntc.pocketweather.util.convertTime
import com.ntc.pocketweather.util.convertTimeToDay
import com.ntc.pocketweather.util.getWeatherIcon
import dagger.hilt.android.AndroidEntryPoint
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

    // google play location service variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Permission callback
    private lateinit var requestPermission: ActivityResultLauncher<String>

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentForecastBinding.bind(view)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        checkLocationSettings()

        requestPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                //permission granted
                if (isGranted) {
                    getNewLocation()
                }
            }

        // defines out location callback which will be called when there are new location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val mostRecentLocation =
                    locationResult.locations[locationResult.locations.size - 1] // get the last (most recent) location in the list

                //get new forecast based on this new location
                viewModel.setNewLocation(mostRecentLocation.latitude, mostRecentLocation.longitude)
                viewModel.getNewForecast()
                stopLocationUpdates()
            }
        }

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

            ivRefresh.setOnClickListener {
                Toast.makeText(requireContext(), "Loading new weather data", Toast.LENGTH_SHORT)
                    .show()
                getNewLocation()
            }

        } // binging.apply{} end

        viewModel.getForecastFromDB().observe(viewLifecycleOwner, {
            // when the forecast changes we need to update the ui
            if (it != null) {
                updateUI(it)

                minutelyAdapter.items = it.minutely
                minutelyAdapter.notifyDataSetChanged()

                hourlyAdapter.items = it.hourly
                hourlyAdapter.notifyDataSetChanged()

                dailyAdapter.items = it.daily
                dailyAdapter.notifyDataSetChanged()
            }
        })

        viewModel.alerts.observe(viewLifecycleOwner, {
            if (it.isNullOrEmpty()) {
                // no alerts so check alert view visibility and background
                if (binding.tvAlert.visibility == View.VISIBLE) {
                    binding.tvAlert.visibility = View.INVISIBLE
                    binding.constraintLayout.setBackgroundResource(R.drawable.normal_layout_background)
                }
            } else {
                // alerts
                if (binding.tvAlert.visibility == View.INVISIBLE) {
                    binding.tvAlert.visibility = View.VISIBLE
                    binding.constraintLayout.setBackgroundResource(R.drawable.alert_layout_background)
                }
            }
        })
    }

    private fun updateUI(foreCast: Forecast) {
        binding.apply {
            // Text
            tvAlert.setOnClickListener {
                if (!viewModel.alerts.value.isNullOrEmpty()) {
                    showWeatherAlertDialog()
                }
            }
            if (!foreCast.alerts.isNullOrEmpty()) {
                viewModel.updateAlerts(foreCast)
            }
            tvCityName.text = "City: ${foreCast.CityName}"
            tvCurrentDescription.text = "${foreCast.current.weather[0].description}"
            tvFeelsLike.text = "Feels like: ${foreCast.current.feels_like}°"
            tvActualTemp.text = "Current Temperature: ${foreCast.current.temp}°"
            tvLat.text = "Lat: ${foreCast.lat}°"
            tvLon.text = "Lon: ${foreCast.lon}°"
            tvSunrise.text = "Sunrise: ${convertTime(foreCast.current.sunrise, true)}"
            tvSunset.text = "Sunset: ${convertTime(foreCast.current.sunset, true)}"
            tvHumidity.text = "Humidity: ${foreCast.current.humidity}%"
            tvWindSpeed.text = "WindSpeed: ${foreCast.current.wind_speed} m/h"
            tvUVI.text = "UVI: ${foreCast.current.uvi}"
            tvVisibility.text = "Visibility: ${foreCast.current.visibility} meters"

            // Icon
            ivWeatherIcon.setImageResource(getWeatherIcon(foreCast.current.weather[0].icon))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        if (checkPermissions()) {
            startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission") // the permission is already checked before this function is called
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder()

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            locationRequest = createLocationRequest()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun showWeatherAlertDialog() {
        var message = ""
        val alertList = viewModel.alerts.value

        for (alert in alertList!!) { // alerts will only show if there is an alert to show (cannot be null)
            message += " - ${alert.description} \n"
        }

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Weather Alert for your area:")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .create()
        dialog.show()
    }

    override fun onDailyClick(daily: Daily) {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.daily_dialog, null)
        val binding = DailyDialogBinding.bind(view)
        val day = convertTimeToDay(daily.dt)

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setView(view)
            .create()

        binding.apply {
            tvTodaysTemp.text = "Temp for $day:"
            tvDailyHigh.text = "High: ${daily.temp.max}"
            tvDailyLow.text = "Low: ${daily.temp.min}"
            tvDailyMorning.text = "Morning: ${daily.temp.morn}"
            tvDailyDay.text = "Mid Day: ${daily.temp.day}"
            tvDailyEvening.text = "Evening: ${daily.temp.eve}"
            tvDailyNight.text = "Night: ${daily.temp.night}"

            tvTodaysFeelLike.text = "Feels Like for $day:"
            tvDailyFLMorning.text = "Morning: ${daily.feels_like.morn}"
            tvDailyFLMidDay.text = "Mid Day: ${daily.feels_like.day}"
            tvDailyFLEvening.text = "Evening: ${daily.feels_like.eve}"
            tvDailyFLNight.text = "Night: ${daily.feels_like.night}"
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}