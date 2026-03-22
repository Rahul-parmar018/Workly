package com.example.workly.ui.admin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdminPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminOverviewFragment()
            1 -> AdminBookingsFragment()
            2 -> AdminProvidersFragment()
            else -> AdminAnalyticsFragment()
        }
    }
}
