package com.example.flowerhunter

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_picture.*
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.jar.Manifest

//get picture
//get brief introduction
//
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
  //  private lateinit var mReport: GoogleMap

  //  private var mLocationRequest: LocationRequest?=null
  //  private val UPDATE_INTERVAL = (10*1000).toLong()
  //  private val FASTEST_INTERVAL: Long = 2000

    private var latitude = 0.0
    private var longitude=0.0
    private lateinit var mLastLocation:Location
    private var mMarker: Marker?=null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object {
        private val MY_PERMISSION_CODE: Int=1000
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if(checkLocationPermission()) {
                buildLocationRequest();
                buildLocationCallBack();
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                )

            }
        } else {
            buildLocationRequest();
            buildLocationCallBack();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper())
        }


       // bottom_navigation_view
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation=p0!!.locations.get(p0.locations.size-1)
                if(mMarker!=null) {
                    mMarker!!.remove()
                }
                latitude=mLastLocation.latitude
                longitude=mLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("my position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                viewNearbyReport()
                mMarker = mMap.addMarker(markerOptions)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
               // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
     //   locationRequest.interval=10000
     //   locationRequest.fastestInterval=5000
     //   locationRequest.smallestDisplacement=10f
    }

    private fun checkLocationPermission(): Boolean {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            }
            return false
        } else
            return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            MY_PERMISSION_CODE->{
                if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                        if(checkLocationPermission()) {
                            buildLocationRequest();
                            buildLocationCallBack();
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper())
                            mMap.isMyLocationEnabled=true
                        }
                    }
                } else {
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
   override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            }
        } else {
            mMap.isMyLocationEnabled=true
        }

        mMap.uiSettings.isZoomControlsEnabled=true
       // mMap.setOnInfoWindowClickListener(this@MapsActivity)
        mMap.setOnInfoWindowClickListener(this)

    }
    private fun viewNearbyReport() {
        // var title:String=""
        // val reportUrl = "https://phase3-257520.appspot.com/view_all_reports"
        //  val report = URL("https://phase3-257520.appspot.com/view_all_reports").readText()
        var report = ""
        var doneFlag = AtomicBoolean(false)
        object : Thread() {
            override fun run() {
                report = URL("https://phase3-257520.appspot.com/view_all_reports").readText()
                doneFlag.set(true)
            }
        }.start()
        while (!doneFlag.get()) {
        }
        val jsonArray = JSONArray(report)
        val reportList = ArrayList<JSONObject>()
        for (i in 0..jsonArray.length() - 1) {
            reportList.add(jsonArray.getJSONObject(i))
        }
        val url = URL("https://phase3-257520.appspot.com/WhiteGoddess")
        val title = ArrayList<String>()
        val content = ArrayList<String>()
        // val theme = ArrayList<JSONObject>()
        for (r in reportList) {
            val num = (1..5).random()
           // val location = LatLng(29.0 + num, -97.0 + num)
            val aux = r.getString("aux")
            var location:LatLng
            if(aux.equals("") || aux.equals("aux") || aux.equals("Austin")) {
                location = LatLng(29.0 + num, -97.0 + num)
            } else {
                val ch = aux.split(" ")
                location = LatLng(ch[1].toDouble(),ch[0].toDouble())
                // Toast.makeText(this,ch[0]+ch[1],Toast.LENGTH_SHORT).show()
            }
            title.add(r.getString("title"))
            content.add(r.getString("description"))


            val theme = r.getString("theme")
            val t = JSONObject(theme.toString())
            val theme_title = t["title"]
            val m = MarkerOptions()
                .position(location)
                .title(r.getString("title")+","+theme_title)
                .snippet(r.getString("description"))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            mMap.addMarker(m)

        }
    }

    override fun onInfoWindowClick(p0: Marker?) {
        val picture = Intent(this@MapsActivity, PictureActivity::class.java)
        picture.putExtra("theme", "theme")
        picture.putExtra("title",p0!!.title)
        picture.putExtra("content",p0.snippet)
        //picture.putExtra("location", p0.position)
        val lat = p0.position.latitude.toString()
        val lng = p0.position.longitude.toString()
        picture.putExtra("latitude", lat)
        picture.putExtra("longitude", lng)
        startActivity(picture)
      //  Toast.makeText(this,"sss",Toast.LENGTH_SHORT).show()
    }

}



