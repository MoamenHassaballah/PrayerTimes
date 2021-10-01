package com.moaapps.prayertimesdemo.utils

import android.app.Activity
import android.util.Log
import com.moaapps.prayertimesdemo.modules.City
import com.moaapps.prayertimesdemo.modules.Country
import com.moaapps.prayertimesdemo.modules.State
import org.json.JSONArray

class LocalLocationsHandler(context: Activity) {
    companion object{ private const val TAG = "LocalLocationsHandler" }
    private val countriesList = ArrayList<Country>()
    init {
        val inputStream = context.assets.open("countries_api.json")
        try {
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes, 0, bytes.size)
            val json = String(bytes)
            setUpData(json)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpData(json:String){
        val countriesArray = JSONArray(json)
        for (countryNumber in 0 until countriesArray.length()){
            val countryObject = countriesArray.getJSONObject(countryNumber)
            val countryId = countryObject.getInt("id")
            val countryName = countryObject.getString("name")
            val countryNative = countryObject.getString("native")
            val countryEmoji = countryObject.getString("emoji")
            val countryStates = ArrayList<State>()

            val statesArray = countryObject.getJSONArray("states")
            for (stateNumber in 0 until statesArray.length()){
                val stateObject = statesArray.getJSONObject(stateNumber)
                val stateId = stateObject.getInt("id")
                val stateName = stateObject.getString("name")
                val stateCode = stateObject.getString("state_code")
                val stateLatitude = stateObject.getString("latitude")
                val stateLongitude = stateObject.getString("longitude")
                val stateCities = ArrayList<City>()

                val citiesArray = stateObject.getJSONArray("cities")
                for (cityNumber in 0 until citiesArray.length()){
                    val cityObject = citiesArray.getJSONObject(cityNumber)
                    val cityId = cityObject.getInt("id")
                    val cityName = cityObject.getString("name")
                    val cityLatitude = cityObject.getString("latitude")
                    val cityLongitude = cityObject.getString("longitude")

                    stateCities.add(City(cityId, cityName, cityLatitude, cityLongitude))
                }

                countryStates.add(State(stateId, stateName, stateCode, stateLatitude, stateLongitude, stateCities))
            }

            countriesList.add(Country(countryId, countryName, countryNative, countryEmoji, countryStates))
        }
    }

    fun getCountries(): ArrayList<Country> {
        return countriesList
    }
}