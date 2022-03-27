package com.fatih.youtube.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.fatih.youtube.R
import com.squareup.picasso.Picasso

const val GOOGLE_SIGN_IN=100
@BindingAdapter("android:downloadImage")
fun downloadImage(view:ImageView,url:String?)
    {
        try {
            url?.let {

                Picasso.get().load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2$url").noFade().placeholder(
                    R.drawable.ic_baseline_account_circle_24)
                    .into(view)
            }
        }catch (e:Exception){

        }
    }
