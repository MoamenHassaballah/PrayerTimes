package com.moaapps.prayertimesdemo.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.moaapps.prayertimesdemo.R

class LoadingDialog(context: Context, type:Int) {

    private var dialog: Dialog = Dialog(context)

    init {
        val locationView = LayoutInflater.from(context).inflate(R.layout.loading_location, null)
        dialog.setCancelable(false)
        dialog.setContentView(locationView)
    }


    fun show(){
        dialog.show()
    }

    fun dismiss(){
        dialog.dismiss()
    }


}