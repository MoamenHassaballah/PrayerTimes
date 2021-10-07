package com.moaapps.prayertimesdemo.background_tasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.tasks.Task
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.utils.Constants
import com.moaapps.prayertimesdemo.utils.TinyDB
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
class GetLocation(val context: Context) {
    companion object {private const val TAG = "GetLocation"}

//    val location = MutableLiveData<Resource<Location>>()
    private var errorMessage: String = ""

    fun getError() : String{
        return errorMessage
    }

    suspend fun getUserLocation() : Location? {
        errorMessage = ""
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {


            val myLocation =  fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken(){
                override fun isCancellationRequested(): Boolean {
                    Log.d(TAG, "isCancellationRequested: ")
                    return true
                }

                override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                    Log.d(TAG, "onCanceledRequested: ")
                    return this
                }

            }).addOnCompleteListener {
                if (it.isSuccessful){
                    setAddress(it.result)
                }else{
                    Log.d(TAG, "getUserLocation error: ${it.exception?.message}")
                    errorMessage = it.exception?.message!!
                }
            }.await()

            return myLocation

        } else{
            errorMessage = context.getString(R.string.error_occured)
            return null
        }
    }


    private fun setAddress(location: Location){
        val addresses = Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)
        val tinyDb = TinyDB(context)

        val country = addresses[0].countryName
        val state = addresses[0].adminArea
        val city = addresses[0].locality

        tinyDb.putString(Constants.COUNTRY, "")
        tinyDb.putString(Constants.STATE, "")
        tinyDb.putString(Constants.CITY, "")

        Log.d(TAG, "setAddress: $city, $state, $country")

        if (!country.isNullOrEmpty()){
            tinyDb.putString(Constants.COUNTRY, country)
            tinyDb.putInt(Constants.COUNTRY_ID, 0)
        }

        if (!state.isNullOrEmpty()) {
            tinyDb.putString(Constants.STATE, state)
            tinyDb.putInt(Constants.STATE_ID, 0)
        }

        if (!city.isNullOrEmpty()) {
            tinyDb.putString(Constants.CITY, city)
            tinyDb.putInt(Constants.CITY_ID, 0)
        }
    }



    private suspend fun <T> Task<T>.await(): T {
        if (isComplete) {
            val e = exception
            return if (e == null) {
                if (isCanceled) {
                    throw CancellationException(
                        "Task $this was cancelled normally.")
                } else {
                    result
                }
            } else {
                throw e
            }
        }

        return suspendCancellableCoroutine { cont ->
            addOnCompleteListener {
                val e = exception
                if (e == null) {
                    if (isCanceled) cont.cancel() else cont.resume(result, {})
                } else {
                    cont.resumeWithException(e)
                }
            }
        }
    }
}