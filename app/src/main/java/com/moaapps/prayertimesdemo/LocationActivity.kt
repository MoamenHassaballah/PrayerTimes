package com.moaapps.prayertimesdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import com.moaapps.prayertimesdemo.databinding.ActivityLocationBinding
import com.moaapps.prayertimesdemo.utils.LoadingDialog
import com.moaapps.prayertimesdemo.utils.Status
import com.moaapps.prayertimesdemo.utils.TinyDB
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
    private lateinit var tinyDb:TinyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this, 1)
        tinyDb = TinyDB(this)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        binding.setManually.setOnToggledListener { _, isOn ->
            if (isOn) {
                binding.autoLocate.isOn = false
                binding.manualLocationLayout.visibility = View.VISIBLE
            } else {
                if (!binding.autoLocate.isOn) {
                    Snackbar.make(binding.root, R.string.select_location_method, Snackbar.LENGTH_SHORT).show()
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
            when(it.status){
                Status.LOADING -> loadingDialog.show()
                Status.FAIL -> {
                    loadingDialog.dismiss()
                    binding.setManually.isOn = true
                    binding.manualLocationLayout.visibility = View.VISIBLE
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
                    Log.d(TAG, "onCreate: ${it.data}")
                }
            }
        })

        binding.saveLocation.setOnClickListener { MainActivity.start(this) }


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
        if (requestCode == 112){

            binding.setManually.isOn = true
            binding.manualLocationLayout.visibility = View.VISIBLE
            Snackbar.make(
                binding.root,
                R.string.couldnt_get_location,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}