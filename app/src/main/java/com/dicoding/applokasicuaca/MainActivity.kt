package com.dicoding.applokasicuaca

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.applokasicuaca.data.WeatherRepository
import com.dicoding.applokasicuaca.data.retrofit.WaetherApiConfig
import com.dicoding.applokasicuaca.databinding.ActivityMainBinding
import com.dicoding.applokasicuaca.utils.TimeUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: WeatherViewModel

    // Permission request launcher
    private val requestPermissionLauncher =

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation() // Fetch location if permission is granted
            } else {
                // Handle permission denied case
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT.also { requestedOrientation = it }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menyembunyikan konten utama dan menampilkan ProgressBar
        binding.mainContent.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check and request location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Initialize WeatherRepository
        val weatherRepository = WeatherRepository(WaetherApiConfig.getApiService())

        // Use the factory
        val viewModelFactory = WeatherViewModelFactory(weatherRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[WeatherViewModel::class.java]

        // LiveData from ViewModel
        viewModel.weatherData.observe(this) { weatherResponse ->
            binding.progressBar.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE

            if (weatherResponse != null) {
                // Update UI
                binding.cityTextView.text = weatherResponse.name
                binding.tempTextView.text =
                    getString(R.string.show_temp, weatherResponse.main?.temp?.roundToInt().toString())
                binding.descriptionTextView.text = weatherResponse.weather?.get(0)?.description

                // Get the icon
                val iconCode = weatherResponse.weather?.get(0)?.icon
                if (iconCode != null) {
                    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                    Glide.with(this)
                        .load(iconUrl)
                        .into(binding.weatherIcon)
                }
            } else {
                // Handle case when data is not available
                binding.cityTextView.text = getString(R.string.data_not_available)
                binding.tempTextView.text = getString(R.string.data_not_available)
                binding.descriptionTextView.text = getString(R.string.data_not_available)
            }
        }

        // Observe LiveData for time data
        viewModel.timeData.observe(this) { time ->
            binding.timeTextView.text = time
        }

        // Observe LiveData for date data
        viewModel.dateData.observe(this) { date ->
            binding.dateTextView.text = date
        }

        // Observe LiveData for time-based message
        viewModel.timeMessageData.observe(this) { message ->
            binding.timeMessageTextView.text = message

            // Memuat ikon berdasarkan waktu
            Glide.with(this)
                .load(TimeUtils.getTimeBasedIconUrl())
                .into(binding.mealIconImageView)

        }

        binding.progressBar.visibility = View.VISIBLE
    }

    // Fetch the last known location
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, cannot fetch location
            return
        }

        // Get the last known location
        Log.d(TAG, "Attempting to get location")
        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task: Task<Location?> ->
                if (task.isSuccessful) {
                    val location = task.result
                    if (location != null) {
                        Log.d(
                            TAG,
                            "Location fetched: Lat: ${location.latitude}, Lon: ${location.longitude}"
                        )
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // Call the weather API with latitude and longitude
                        fetchWeatherByCoordinates(latitude, longitude)
                    } else {
                        // Handle case when location is null
                        Log.d(TAG, "Location is null")
                    }
                } else {
                    // Handle failure to get location
                    Log.d(TAG, "Failed to get location: ${task.exception?.message}")
                }
            }
    }

    // Fetch weather using latitude and longitude
    private fun fetchWeatherByCoordinates(latitude: Double, longitude: Double) {
        val apiKey = BuildConfig.API_KEY
        Log.d(TAG, "API request...........")
        viewModel.fetchWeatherByCoordinates(latitude, longitude, apiKey)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
