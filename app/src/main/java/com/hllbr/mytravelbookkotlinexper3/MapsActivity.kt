package com.hllbr.mytravelbookkotlinexper3

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hllbr.mytravelbookkotlinexper3.databinding.ActivityMapsBinding
import java.util.*
import java.util.jar.Manifest
import kotlin.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intentToMain = Intent(this,MainActivity::class.java)
        startActivity(intentToMain)
        finish()

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(myListener)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if(location != null){
                    val sharedPreferences = this@MapsActivity.getSharedPreferences("com.hllbr.mytravelbookkotlinexper3",
                        Context.MODE_PRIVATE)
                    val firstTimeCheck = sharedPreferences.getBoolean("notFirstTime",false)
                    if (!firstTimeCheck){
                        mMap.clear()
                        val newUserLoaciton = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newUserLoaciton,15f))
                        sharedPreferences.edit().putBoolean("notFirstTime",true).apply()
                    }

                }
            }

        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //ı have not permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            //requestcode is used to check what build the permission comes from
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
            val intent = intent
            val infos = intent.getStringExtra("infos")
            if(infos.equals("new")){
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastLocation != null){
                    val lastLocationLatLng = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng,15f))
                    //ı did not use addMarker
            }

            }else{
                mMap.clear()
                val selectedPlace = intent.getSerializableExtra("selectedPlace") as Place
                val selectedLocation = LatLng(selectedPlace.latitude!!,selectedPlace.longitude!!)
                mMap.addMarker(MarkerOptions().title(selectedPlace.address).position(selectedLocation))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation,15f))

            }
        }
    }
    val myListener = object : GoogleMap.OnMapLongClickListener{
        override fun onMapLongClick(p0: LatLng) {
            val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            var address = ""
            if (p0 != null){
                try{
                    val addresslist = geocoder.getFromLocation(p0.latitude,p0.longitude,1)
                    if (addresslist != null && addresslist.size>0){
                        if (addresslist[0].thoroughfare != null){
                            address += addresslist[0].thoroughfare
                            if (addresslist[0].subThoroughfare != null){
                                address += addresslist[0].subThoroughfare
                            }
                        }
                    }else{
                        address = "New Place"
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(p0).title(address))
                val newPlace = Place(address,p0.latitude,p0.longitude)
                val dialog = AlertDialog.Builder(this@MapsActivity)
                dialog.setCancelable(false)
                dialog.setTitle("Are You Sure ¿")
                dialog.setMessage(newPlace.address)
                dialog.setPositiveButton("YES"){dialog,which ->
                    //SQLite Save
                    try {
                        val database =openOrCreateDatabase("PLACES",Context.MODE_PRIVATE,null)
                        database.execSQL("CREATE TABLE IF NOT EXISTS places(address VARCHAR,latitude Double,longitude Double)")
                        //değerleri başka bir yerden aldığımda sqlStatement olarak ifade edilen bir yapıdan yararlanıyorum
                        val toCompile = "INSERT INTO places(address,latitude,longitude) VALUES (?,?,?)"
                        val sqliteStatement = database.compileStatement(toCompile)
                        sqliteStatement.bindString(1,newPlace.address)
                        sqliteStatement.bindDouble(2,newPlace.latitude!!)
                        sqliteStatement.bindDouble(3,newPlace.longitude!!)
                        sqliteStatement.execute()

                    }catch (e :Exception){
                        e.printStackTrace()
                    }

                    Toast.makeText(this@MapsActivity,"new place created",Toast.LENGTH_LONG).show()

                }.setNegativeButton("NO"){dialog,which->
                    Toast.makeText(this@MapsActivity,"Cancelled!",Toast.LENGTH_LONG).show()
                }
                dialog.show()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1 && grantResults.size>0){
            if(ContextCompat.checkSelfPermission(
                    this,android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED){
                    //cannot be the last known location when permission is first granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}