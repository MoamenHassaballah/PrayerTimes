package com.moaapps.prayertimesdemo.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.adapters.CountriesSpinnerAdapter
import com.moaapps.prayertimesdemo.databinding.ActivityLocationBinding
import com.moaapps.prayertimesdemo.modules.City
import com.moaapps.prayertimesdemo.modules.Country
import com.moaapps.prayertimesdemo.modules.State
import com.moaapps.prayertimesdemo.utils.*
import com.moaapps.prayertimesdemo.utils.Constants.CITY
import com.moaapps.prayertimesdemo.utils.Constants.CITY_ID
import com.moaapps.prayertimesdemo.utils.Constants.COUNTRY
import com.moaapps.prayertimesdemo.utils.Constants.COUNTRY_ID
import com.moaapps.prayertimesdemo.utils.Constants.LATITUDE
import com.moaapps.prayertimesdemo.utils.Constants.LOCATION_METHOD
import com.moaapps.prayertimesdemo.utils.Constants.LOCATION_METHOD_AUTO
import com.moaapps.prayertimesdemo.utils.Constants.LOCATION_METHOD_MANUAL
import com.moaapps.prayertimesdemo.utils.Constants.LONGITUDE
import com.moaapps.prayertimesdemo.utils.Constants.STATE
import com.moaapps.prayertimesdemo.utils.Constants.STATE_ID
import com.moaapps.prayertimesdemo.viewmodel.LocationViewModel
import pub.devrel.easypermissions.EasyPermissions

class LocationActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, LocationActivity::class.java)
            context.startActivity(starter)
        }

        private const val TAG = "LocationActivity"
    }

    private lateinit var binding: ActivityLocationBinding
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var tinyDb: TinyDB
    private lateinit var localLocationsHandler: LocalLocationsHandler
    private lateinit var countriesList: List<Country>
    private lateinit var selectedCountry: Country
    private lateinit var selectedState: State
    private lateinit var selectedCity: City

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this, 1)
        tinyDb = TinyDB(this)

        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        binding.setManually.setOnToggledListener { _, isOn ->
            if (isOn) {
                loadManualLocationData()
                binding.autoLocate.isOn = false
                binding.manualLocationLayout.visibility = View.VISIBLE
            } else {
                if (!binding.autoLocate.isOn) {
                    Snackbar.make(
                        binding.root,
                        R.string.select_location_method,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    binding.setManually.isOn = true
                }
            }
        }

        binding.autoLocate.setOnToggledListener { _, isOn ->
            if (isOn) {
                if (!EasyPermissions.hasPermissions(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.location_rational),
                        112,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    binding.autoLocate.isOn = false
                } else {
                    locationViewModel.getUserLocation(this)
                }

            } else {
                if (!binding.setManually.isOn) {
                    Toast.makeText(this, R.string.select_location_method, Toast.LENGTH_SHORT).show()
                    binding.autoLocate.isOn = true
                }
            }
        }





        locationViewModel.location.observe(this, {
            when (it.status) {
                Status.LOADING -> loadingDialog.show()
                Status.FAIL -> {
                    loadingDialog.dismiss()
                    binding.setManually.isOn = true
                    binding.manualLocationLayout.visibility = View.VISIBLE
                    binding.autoLocate.isOn = false
                    Snackbar.make(
                        binding.root,
                        R.string.couldnt_get_location,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                Status.SUCCESSFUL -> {
                    loadingDialog.dismiss()
                    binding.setManually.isOn = false
                    binding.manualLocationLayout.visibility = View.GONE

                    tinyDb.putString(LOCATION_METHOD, LOCATION_METHOD_AUTO)
                    tinyDb.putDouble(LATITUDE, it.data?.latitude!!)
                    tinyDb.putDouble(LONGITUDE, it.data.longitude)

                    Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT).show()
                    MainActivity.start(this)
                }
            }
        })


        if (tinyDb.getString(LOCATION_METHOD) == LOCATION_METHOD_AUTO) {
            binding.autoLocate.isOn = true
            binding.setManually.isOn = false
            binding.manualLocationLayout.visibility = View.GONE
        } else {
            loadManualLocationData()
            binding.setManually.isOn = true
            binding.manualLocationLayout.visibility = View.VISIBLE
            binding.autoLocate.isOn = false
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == 112) {
            locationViewModel.getUserLocation(this)
        }

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == 112) {

            binding.setManually.isOn = true
            binding.manualLocationLayout.visibility = View.VISIBLE
            Snackbar.make(
                binding.root,
                R.string.couldnt_get_location,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


    private fun getLocalLocationsList(country: Country, state: State): List<ArrayList<String>> {
        val statesList = country.states
        val citiesList = state.cities

        val statesStringList = ArrayList<String>()
        for (st in statesList) {
            statesStringList.add(st.name)
        }

        val cityStringList = ArrayList<String>()
        for (city in citiesList) {
            cityStringList.add(city.name)
        }

        return listOf(statesStringList, cityStringList)
    }


    private fun loadManualLocationData() {
        loadingDialog.show()
        Thread {
            localLocationsHandler = LocalLocationsHandler(this)
            countriesList = localLocationsHandler.getCountries()

            runOnUiThread {

                val countriesAdapter = CountriesSpinnerAdapter(countriesList)
                var dataList = getLocalLocationsList(countriesList[0], countriesList[0].states[0])

                binding.spinnerCountries.adapter = countriesAdapter
                binding.spinnerStates.adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataList[0])
                binding.spinnerCities.adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataList[1])


                binding.spinnerCountries.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            Log.d(TAG, "onItemSelectedCountry: $p2")
                            selectedCountry = countriesList[p2]
                            dataList =
                                getLocalLocationsList(selectedCountry, selectedCountry.states[0])
                            binding.spinnerStates.adapter = ArrayAdapter(
                                this@LocationActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                dataList[0]
                            )
                            binding.spinnerCities.adapter = ArrayAdapter(
                                this@LocationActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                dataList[1]
                            )
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                    }


                binding.spinnerStates.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            Log.d(TAG, "onItemSelectedState: $p2")
                            selectedState = selectedCountry.states[p2]
                            dataList = getLocalLocationsList(selectedCountry, selectedState)
                            binding.spinnerCities.adapter = ArrayAdapter(
                                this@LocationActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                dataList[1]
                            )
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                    }

                binding.spinnerCities.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            Log.d(TAG, "onItemSelectedCity: $p2")
                            selectedCity = selectedState.cities[p2]
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }

                    }

                binding.spinnerCountries.setSelection(tinyDb.getInt(COUNTRY_ID))
            }
            Thread.sleep(1000)
            runOnUiThread { binding.spinnerStates.setSelection(tinyDb.getInt(STATE_ID)) }
            Thread.sleep(1000)
            runOnUiThread {
                binding.spinnerCities.setSelection(tinyDb.getInt(CITY_ID))
                binding.saveLocation.setOnClickListener {
                    tinyDb.putString(LOCATION_METHOD, LOCATION_METHOD_MANUAL)
                    tinyDb.putString(COUNTRY, selectedCountry.name)
                    tinyDb.putInt(COUNTRY_ID, countriesList.indexOf(selectedCountry))
                    tinyDb.putString(STATE, selectedState.name)
                    tinyDb.putInt(STATE_ID, selectedCountry.states.indexOf(selectedState))
                    if (this::selectedCity.isInitialized){
                        tinyDb.putDouble(LATITUDE, selectedCity.latitude.toDouble())
                        tinyDb.putDouble(LONGITUDE, selectedCity.longitude.toDouble())
                        tinyDb.putString(CITY, selectedCity.name)
                        tinyDb.putInt(CITY_ID, selectedState.cities.indexOf(selectedCity))
                    }else{
                        tinyDb.putDouble(LATITUDE, selectedState.latitude.toDouble())
                        tinyDb.putDouble(LONGITUDE, selectedState.longitude.toDouble())
                        tinyDb.putString(CITY, "")
                        tinyDb.putInt(CITY_ID, 0)
                    }

                    Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT).show()
                    MainActivity.start(this)
                }

                loadingDialog.dismiss()

            }
        }.start()


    }

}