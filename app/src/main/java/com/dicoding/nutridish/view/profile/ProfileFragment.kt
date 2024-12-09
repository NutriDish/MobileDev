package com.dicoding.nutridish.view.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.main.MainActivity
import com.dicoding.nutridish.view.profile.settings.AboutActivity
import com.dicoding.nutridish.view.profile.settings.UpdateProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userSnapshotListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            userSnapshotListener = firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(), "Gagal memuat data pengguna.", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("userName") ?: "Unknown"
                        val email = snapshot.getString("email") ?: "Unknown"

                        // Update UI
                        view.findViewById<TextView>(R.id.textName).text = name
                        view.findViewById<TextView>(R.id.textEmail).text = email
                    }
                }
        }

        val buttonUpdate = view.findViewById<View>(R.id.btnUpdateProfile)
        val buttonAbout = view.findViewById<View>(R.id.btnAboutApp)
        val buttonLogout = view.findViewById<View>(R.id.btnLogout)

        val viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(requireActivity()))[ProfileViewModel::class.java]

        buttonAbout.setOnClickListener{
            val intent = Intent(activity, AboutActivity::class.java)
            startActivity(intent)
        }
        buttonUpdate.setOnClickListener{
            val intent = Intent(activity, UpdateProfileActivity::class.java)
            startActivity(intent)
        }
        buttonLogout.setOnClickListener{
            lifecycleScope.launch {
                viewModel.logout()
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userSnapshotListener?.remove()
    }

}