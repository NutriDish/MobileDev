package com.dicoding.nutridish.view.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dicoding.nutridish.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.dicoding.nutridish.notification.NotificationHelper
import com.dicoding.nutridish.notification.NotificationReceiver


class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val apiKey = "ff948dc9af3f8c4ab8c01619e3ac0eb1"

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleMealNotifications()

        val textTime = view.findViewById<TextView>(R.id.textTime)
        val textTemperature = view.findViewById<TextView>(R.id.textTemperature)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        val textGreeting = view.findViewById<TextView>(R.id.textGreeting)
        if (currentUser != null) {
            val userId = currentUser.uid

            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("userName") ?: "User"
                        textGreeting.text = "Hi, $name"
                    } else {
                        textGreeting.text = "Hi, User"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memuat data pengguna.", Toast.LENGTH_SHORT).show()
                }
        } else {
            textGreeting.text = "Hi, Guest"
        }

        setupTimeUpdates(textTime)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentTemperature(textTemperature)
        } else {
            requestLocationPermission()
        }
    }

    private fun setupTimeUpdates(textTime: TextView) {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val currentTime = Calendar.getInstance().time
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                textTime.text = timeFormat.format(currentTime)
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(runnable)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentTemperature(textTemperature: TextView) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fetchTemperature(location.latitude, location.longitude, textTemperature)
            } else {
                textTemperature.text = "N/A"
            }
        }
    }

    private fun fetchTemperature(lat: Double, lon: Double, textTemperature: TextView) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)
        weatherApi.getCurrentWeather(lat, lon, apiKey, "metric").enqueue(object : retrofit2.Callback<WeatherResponse> {
            override fun onResponse(call: retrofit2.Call<WeatherResponse>, response: retrofit2.Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val temp = response.body()?.main?.temp ?: 0.0
                    val formattedTemp = "${temp.toInt()}°C"
                    textTemperature.text = formattedTemp
                } else {
                    textTemperature.text = "N/A"
                }
            }

            override fun onFailure(call: retrofit2.Call<WeatherResponse>, t: Throwable) {
                textTemperature.text = "N/A"
            }
        })
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

    data class WeatherResponse(
        val main: Main
    )

    data class Main(
        val temp: Double
    )

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val textTemperature = view?.findViewById<TextView>(R.id.textTemperature)
                textTemperature?.let { getCurrentTemperature(it) }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleMealNotifications() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if the app has permission to schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canScheduleExactAlarms = alarmManager.canScheduleExactAlarms()
            if (!canScheduleExactAlarms) {
                // Request permission or show an alert
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val notificationHelper = NotificationHelper(requireContext())

        val mealTimes = listOf(
            Pair(7, "Breakfast time! Don't forget to eat."),
            Pair(13, "Lunch time! It's important to stay energized."),
            Pair(19, "Dinner time! End your day with a good meal.")
        )

        for ((hour, message) in mealTimes) {
            val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
                putExtra("NOTIFICATION_TITLE", "Meal Reminder")
                putExtra("NOTIFICATION_MESSAGE", message)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                hour,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Ensure compatibility with Android 12 and later
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1) // Schedule for the next day if time has passed
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
