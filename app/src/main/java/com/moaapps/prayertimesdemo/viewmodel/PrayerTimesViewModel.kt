package com.moaapps.prayertimesdemo.viewmodel

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants.LATITUDE
import com.moaapps.prayertimesdemo.utils.Constants.LOCATION_METHOD
import com.moaapps.prayertimesdemo.utils.Constants.LOCATION_METHOD_AUTO
import com.moaapps.prayertimesdemo.utils.Constants.LOCATION_METHOD_MANUAL
import com.moaapps.prayertimesdemo.utils.Constants.LONGITUDE
import com.moaapps.prayertimesdemo.utils.Constants.TIMINGS
import com.moaapps.prayertimesdemo.utils.Resource
import com.moaapps.prayertimesdemo.utils.Status
import com.moaapps.prayertimesdemo.utils.TinyDB
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PrayerTimesViewModel : ViewModel(){
    companion object{private const val TAG = "PrayerTimesViewModel"}

    val prayerTimes = MutableLiveData<Resource<PrayerTimes>>()
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var tinyDb: TinyDB

    fun getPrayerTimes(context: AppCompatActivity){
        Log.d(TAG, "getPrayerTimes: ")
        tinyDb = TinyDB(context)
        if (tinyDb.getString(LOCATION_METHOD) == LOCATION_METHOD_AUTO){
            locationViewModel = ViewModelProvider(context)[LocationViewModel::class.java]
            locationViewModel.location.observe(context, {
                when(it.status){
                    Status.LOADING -> prayerTimes.postValue(Resource.loading())
                    Status.FAIL -> {
                        getTimes(context, tinyDb.getDouble(LATITUDE), tinyDb.getDouble(LONGITUDE))
                    }
                    Status.SUCCESSFUL -> {
                        getTimes(context, it.data?.latitude!!, it.data.longitude)
                    }
                }
            })
            locationViewModel.getUserLocation(context)
        }else if (tinyDb.getString(LOCATION_METHOD) == LOCATION_METHOD_MANUAL){
            getTimes(context, tinyDb.getDouble(LATITUDE), tinyDb.getDouble(LONGITUDE))
        }
    }

    private fun getTimes(context: AppCompatActivity, lat: Double, long: Double){
        prayerTimes.postValue(Resource.loading())
        val url = "http://api.aladhan.com/v1/timings?latitude=$lat&longitude=$long"

        AndroidNetworking.get(url)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response != null){
                        val timings = response.getJSONObject("data").getJSONObject("timings")
                        prayerTimes.postValue(Resource.success(PrayerTimes(timings.getString("Fajr"),
                            timings.getString("Dhuhr"), timings.getString("Asr"),
                            timings.getString("Maghrib"), timings.getString("Isha"))))
//                        tinyDb.putString(TIMINGS, Gson().toJson(prayerTimes))
                        Log.d(TAG, "onResponse: $response")
                    }else{
                        prayerTimes.postValue(Resource.error(context.getString(R.string.error_occured)))
                    }

                }

                override fun onError(anError: ANError?) {
                    anError?.printStackTrace()
                    if (anError != null && !anError.message.isNullOrEmpty()) {
                        prayerTimes.postValue(Resource.error(anError.message!!))
                    }else{
                        prayerTimes.postValue(Resource.error(context.getString(R.string.error_occured)))
                    }
                }
            })
    }
}