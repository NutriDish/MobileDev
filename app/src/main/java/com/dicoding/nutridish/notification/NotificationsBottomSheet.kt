package com.dicoding.nutridish.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.nutridish.R
import com.dicoding.nutridish.data.database.room.NutriDatabase
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class NotificationsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationDao by lazy {
        NutriDatabase.getInstance(requireContext()).notificationDao()
    }
    private val viewModel: NotificationsViewModel by viewModels {
        NotificationsViewModelFactory(notificationDao)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_notifications_bottom_sheet, container, false)
        setupRecyclerView(view)
        observeNotifications()
        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_notifications)
        notificationAdapter = NotificationAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = notificationAdapter
    }

    private fun observeNotifications() {
        lifecycleScope.launch {
            viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
                notificationAdapter.submitList(notifications)
            }
        }
    }
}
