package com.moaapps.prayertimesdemo.viewmodel

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.moaapps.prayertimesdemo.utils.Constants
import com.moaapps.prayertimesdemo.utils.Constants.CITY
import com.moaapps.prayertimesdemo.utils.Constants.CITY_ID
import com.moaapps.prayertimesdemo.utils.Constants.COUNTRY
import com.moaapps.prayertimesdemo.utils.Constants.COUNTRY_ID
import com.moaapps.prayertimesdemo.utils.Constants.STATE
import com.moaapps.prayertimesdemo.utils.Constants.STATE_ID
import com.moaapps.prayertimesdemo.utils.Resource
import com.moaapps.prayertimesdemo.utils.TinyDB

class LocationViewModel : ViewModel() {
    companion object {private const val TAG = "LocationViewModel"}

    val location = MutableLiveData<Resource<Location>>()

    fun getUserLocation(context: Activity){
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
                    Log.d(TAG, "getUserLocation: ${it.result}")
                    if(it.result != null){
                        setAddress(context, it.result)
                        location.value = Resource.success(it.result)
                    }else{
                        location.value = Resource.error()
                    }
                } else {
                    Log.d(TAG, "getUserLocation: ${it.exception?.message}")
                    location.value = Resource.error()
                }
            }
        }
    }


    private fun setAddress(context: Activity, location: Location){
        val addresses = Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)
        val tinyDb = TinyDB(context)

        val country = addresses[0].countryName
        val state = addresses[0].adminArea
        val city = addresses[0].locality

        Log.d(TAG, "setAddress: $city, $state, $country")

        if (!country.isNullOrEmpty()){
            tinyDb.putString(COUNTRY, country)
            tinyDb.putInt(COUNTRY_ID, 0)
        }

        if (!state.isNullOrEmpty()) {
            tinyDb.putString(STATE, state)
            tinyDb.putInt(STATE_ID, 0)
        }

        if (!city.isNullOrEmpty()) {
            tinyDb.putString(CITY, city)
            tinyDb.putInt(CITY_ID, 0)
        }
    }

}