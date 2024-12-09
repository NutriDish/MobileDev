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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        val name = document.getString("name") ?: "User"
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
}
