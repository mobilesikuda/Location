package ru.sikuda.mobile.loc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import ru.sikuda.mobile.loc.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    //Clear location
    private lateinit var locationManagerGPS: LocationManager
    private lateinit var locationManagerNet: LocationManager

    //And Google services
    private var fLocationAvailable: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    //permission for location
    private val PERMISSION_REQUEST1 = 1001
    private val PERMISSION_REQUEST2 = 1002

    //main binding
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        locationManagerGPS = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManagerNet = getSystemService(LOCATION_SERVICE) as LocationManager

        fLocationAvailable = isLocationEnabled(this)
        if (fLocationAvailable) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val locationResult1 = locationResult ?: return
                    for (location in locationResult1.locations) {
                        showLocation(location)
                    }
                }
            }
        }
        binding.switchGPS.isClickable = false
        binding.switchNet.isClickable = false
        binding.switchGoogle.isClickable = false
        binding.switchGoogle.isChecked = fLocationAvailable
    }

    @SuppressLint("ObsoleteSdkInt")
    fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This was deprecated in API 28
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED )
            if (binding.switchGPS.isChecked)
                locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10f, locationListenerGPS )
        else ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST1
        )

        if( ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            if(binding.switchNet.isChecked)
                locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10f, locationListenerNet)
        else ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST2
        )

        //Google services
        startfusedUpdates()

        checkEnabled()
    }

    private fun startfusedUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedRequest  = LocationRequest.create()
            fusedLocationClient.requestLocationUpdates( fusedRequest,
                    locationCallback,
                    Looper.getMainLooper())
        }

    }

    override fun onPause() {
        locationManagerGPS.removeUpdates(locationListenerGPS)
        locationManagerNet.removeUpdates(locationListenerNet)
        fusedLocationClient.removeLocationUpdates(locationCallback)

        super.onPause()
    }

    //GPS location
    private val locationListenerGPS: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
            checkEnabled()
        }

        override fun onProviderEnabled(provider: String) {
            checkEnabled()
            if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED )  {
                showLocation(locationManagerGPS.getLastKnownLocation(provider))
            }

        }
    }

    //Net location
    private val locationListenerNet: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
            checkEnabled()
        }

        override fun onProviderEnabled(provider: String) {
            checkEnabled()
            if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ) {
                showLocation(locationManagerNet.getLastKnownLocation(provider))
            }
        }
    }

    private fun showLocation(location: Location?) {
        if (location?.provider == LocationManager.GPS_PROVIDER)
            binding.tvLocationGPS.setText(formatLocation( location )
            )
        else if (location?.provider == LocationManager.NETWORK_PROVIDER )
            binding.tvLocationNet.setText(formatLocation(location))
        else
            binding.tvLocationGoogle.setText(formatLocation(location))
    }

    private fun formatLocation(location: Location?): String {
        return if (location == null) "-"
        else String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3\$tF %3\$tT",
                location.latitude, location.longitude, Date(location.time)
        )
    }

    private fun checkEnabled() {

        binding.switchGPS.isChecked = locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER)
        binding.switchNet.isChecked = locationManagerNet.isProviderEnabled(LocationManager.GPS_PROVIDER)

    }

}




