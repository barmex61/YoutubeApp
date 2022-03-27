package com.fatih.youtube.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.fatih.youtube.dashboards.*

class ViewPagerAdapter(fm:FragmentManager): FragmentPagerAdapter(fm) {



    override fun getCount(): Int {
        return 5
    }

    override fun getItem(position: Int): Fragment {
        return when(position){

            0->{ HomeDashboardFragment() }
            1->{ VideosDashboardFragment() }
            2->{ PlaylistDashboardFragment() }
            3->{ SubscriptionDashboardFragment() }
            4->{ AboutDashboardFragment() }
            else->{ HomeDashboardFragment() }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when(position){
            0->{ "Home"}
            1->{ "Videos"}
            2->{"Playlist"}
            3->{"Subscription"}
            4->{"About"}
            else->{"Home"}
        }
    }
}