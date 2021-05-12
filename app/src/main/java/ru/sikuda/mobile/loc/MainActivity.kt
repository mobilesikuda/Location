package ru.sikuda.mobile.loc

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var fusedLocationUpdates = true
    private lateinit var locationCallback: LocationCallback


    private val PERMISSION_REQUEST1 = 1001
    private val PERMISSION_REQUEST2 = 1002

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        locationManagerGPS = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManagerNet = getSystemService(LOCATION_SERVICE) as LocationManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    showLocation(location)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED )
            //if(locationManagerGPS.getAllProviders().contains(LocationManager.GPS_PROVIDER) && locationManagerNet.isProviderEnabled(LocationManager.GPS_PROVIDER))
                locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10f, locationListenerGPS )
        else ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST1
        )

        if( ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            if(locationManagerNet.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManagerNet.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10f, locationListenerNet);
            //locationManagerNet.requestLocationUpdates(
            //        LocationManager.NETWORK_PROVIDER, 1000 * 10, 10f,
            //        locationListenerNet
            //)
        else ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST2
        )

        //Google services
        if (fusedLocationUpdates) startfusedUpdates()

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
                showLocation(locationManagerGPS!!.getLastKnownLocation(provider))
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

    private fun formatLocation(location: Location?): String? {
        return if (location == null) "-"
        else String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3\$tF %3\$tT",
                location.latitude, location.longitude, Date(location.time)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun checkEnabled() {
        binding.tvEnabledGPS.setText( """GPS Enabled: ${locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER)}""")
        binding.tvEnabledNet.setText( """Network Enabled: ${locationManagerNet.isProviderEnabled(LocationManager.NETWORK_PROVIDER)}""")
    }

}


