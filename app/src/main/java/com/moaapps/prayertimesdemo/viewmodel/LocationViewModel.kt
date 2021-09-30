package com.moaapps.prayertimesdemo.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import com.moaapps.prayertimesdemo.LocationActivity
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.utils.Resource

class LocationViewModel : ViewModel() {
    companion object {private const val TAG = "LocationViewModel"}

    val location = MutableLiveData<Resource<Location>>()

    fun getUserLocation(context: Context){
        location.postValue(Resource.loading())
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken(){
                override fun isCancellationRequested(): Boolean {
                    Log.d(TAG, "isCancellationRequested: ")
                    return true
                }

                override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                    Log.d(TAG, "onCanceledRequested: ")
                    return this
                }

            }).addOnCompleteListener {
                if (it.isSuccessful) {
                    if(it.result != null){
                        location.postValue(Resource.success(it.result))
                    }else{
                        location.postValue(Resource.error())
                    }
                } else {
                    location.postValue(Resource.error())
                }
            }
        }
    }

}