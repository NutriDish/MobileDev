package com.dicoding.nutridish.view.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.nutridish.R
import com.dicoding.nutridish.ViewModelFactory
import com.dicoding.nutridish.login.LoginActivity
import com.dicoding.nutridish.view.profile.settings.AboutActivity
import com.dicoding.nutridish.view.profile.settings.UpdateProfileActivity
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

        }
    }

}