package com.fatih.youtube.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fatih.youtube.R
import com.fatih.youtube.adapter.ViewPagerAdapter
import com.fatih.youtube.databinding.FragmentChannelDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChannelDashboardFragment:Fragment(R.layout.fragment_channel_dashboard) {

    private lateinit var binding:FragmentChannelDashboardBinding
    private lateinit var fireStore:FirebaseFirestore
    private lateinit var adapter:ViewPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= FragmentChannelDashboardBinding.inflate(inflater,container,false)
        doInit()
        return binding.root
    }
    private fun doInit(){
        initAdapter()
        fireStore= FirebaseFirestore.getInstance()
        fireStore.collection("Channels").document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener{value,error->
            error?.let {
                Toast.makeText(requireContext(),error.message,Toast.LENGTH_SHORT).show()
            }?:if(value!=null&&value.exists()){
                binding.ChannelName.text=value["channelName"].toString()

            }else{
                Toast.makeText(requireContext(),"Data not found",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initAdapter() {
        adapter=ViewPagerAdapter(requireActivity().supportFragmentManager)
        binding.viewPager.adapter=adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}