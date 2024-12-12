package com.dicoding.nutridish.view.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.data.api.response.ResponseItem
import com.dicoding.nutridish.databinding.FragmentDashboardBinding
import com.dicoding.nutridish.notification.NotificationHelper
import com.dicoding.nutridish.notification.NotificationReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build.VERSION_CODES

class DashboardFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recentlyAdapter: DashboardAdapter

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupGreeting()
        setupRecyclerView()
        setupTimeUpdates()
        checkLocationPermissionAndFetchTemperature()

        scheduleMealNotifications()
    }

    private fun setupGreeting() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val userName = document?.getString("userName") ?: "User"
                    binding.textGreeting.text = "Hi, $userName"
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memuat data pengguna.", Toast.LENGTH_SHORT).show()
                    binding.textGreeting.text = "Hi, User"
                }
        } else {
            binding.textGreeting.text = "Hi, Guest"
        }
    }

    private fun setupRecyclerView() {
        val factory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: DashboardViewModel by viewModels { factory }

        recentlyAdapter = DashboardAdapter()

        viewModel.getFavoriteRecipe.observe(viewLifecycleOwner) { recentlyList ->
            if (recentlyList.isNullOrEmpty()) {
                Log.d("DashboardFragment", "Recently list is empty")
            } else {
                val items = recentlyList.map {
                    ResponseItem(title = it.title)
                }
                recentlyAdapter.submitList(items)
            }
        }

        binding.recyclerViewRecentlyAdded.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentlyAdapter
        }

    }

    private fun setupTimeUpdates() {
        lifecycleScope.launch {
            while (isAdded) {
                val currentTime = Calendar.getInstance().time
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.textTime.text = timeFormat.format(currentTime)
                delay(1000)
            }
        }
    }


    private fun checkLocationPermissionAndFetchTemperature() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentTemperature()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentTemperature() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fetchTemperature(location.latitude, location.longitude)
            } else {
                binding.textTemperature.text = "N/A"
            }
        }
    }

    private fun fetchTemperature(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)
        weatherApi.getCurrentWeather(lat, lon, API_KEY, "metric").enqueue(object :
            retrofit2.Callback<WeatherResponse> {
            override fun onResponse(
                call: retrofit2.Call<WeatherResponse>,
                response: retrofit2.Response<WeatherResponse>
            ) {
                val temp = response.body()?.main?.temp ?: 0.0
                binding.textTemperature.text = "${temp.toInt()}°C"
            }

            override fun onFailure(call: retrofit2.Call<WeatherResponse>, t: Throwable) {
                binding.textTemperature.text = "N/A"
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleMealNotifications() {
        if (!isNotificationPermissionGranted()) {
            requestNotificationPermission()
            return
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
            Toast.makeText(
                requireContext(),
                "Silakan izinkan aplikasi untuk menjadwalkan alarm yang tepat.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val mealTimes = listOf(
            Pair(7, "Breakfast time! Don't forget to eat."),
            Pair(13, "Lunch time! It's important to stay energized."),
            Pair(19, "Dinner time! End your day with a good meal.")
        )

        for ((hour, message) in mealTimes) {
            try {
                val intent = NotificationReceiver.createIntent(requireContext(), "Meal Reminder", message)
                val pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    hour,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }

                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Gagal menjadwalkan alarm: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    interface WeatherApi {
        @GET("weather")
        fun getCurrentWeather(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("appid") apiKey: String,
            @Query("units") units: String
        ): retrofit2.Call<WeatherResponse>
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 2
        private const val API_KEY = "ff948dc9af3f8c4ab8c01619e3ac0eb1"
    }

    data class WeatherResponse(
        val main: Main
    )

    data class Main(
        val temp: Double
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    @RequiresApi(VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    scheduleMealNotifications()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Notifikasi dinonaktifkan. Anda akan melewatkan pengingat makanan.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}


