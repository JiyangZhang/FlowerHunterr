package com.example.flowerhunter

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.*
import com.google.android.gms.maps.OnMapReadyCallback
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.JsonObject
import dmax.dialog.SpotsDialog
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback
import com.example.flowerhunter.Common.Common


class ViewDirections : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mLastLocation: Location
    private var latitude = 0.0
    private var longitude=0.0
    private var mMarker: Marker?=null

    private var des_latitude = 0.0
    private var des_longitude = 0.0

    lateinit var mService: IGoogleAPIService
    // location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    var polyLine: Polyline? = null

    companion object {
        private val MY_PERMISSION_CODE: Int=1000
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_directions)
        val bundle = intent.extras
        des_latitude = bundle!!.getString("latitude")!!.toDouble()
        des_longitude = bundle!!.getString("longitude")!!.toDouble()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // get the location of destination

        mService = Common.googleApiServiceScalars
        // asking for runtime permission
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
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
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            ViewDirections.MY_PERMISSION_CODE ->{
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
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



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
        //mMap.setOnInfoWindowClickListener(this)

    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                // add your location to map
                //mLastLocation=p0!!.lastLocation  // Get last location
                mLastLocation=p0!!.locations.get(p0.locations.size-1)
                if(mMarker!=null) {
                    mMarker!!.remove()
                }

                latitude=mLastLocation.latitude
                longitude=mLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your  position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))


                mMarker = mMap!!.addMarker(markerOptions)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(13f))
                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17f))

                // create marker for destination
                /*
                val destinationLatLng = LatLng(Common.currentResult!!.geometry!!.location.lat.toDouble(),
                    Common.currentResult!!.geometry!!.location.lng.toDouble())
                mMap!!.addMarker(MarkerOptions().position(destinationLatLng)
                    .title(Common.currentResult!!.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                   */

                val destinationLatLng = LatLng(des_latitude, des_longitude)
                mMap!!.addMarker(MarkerOptions()
                    .position(destinationLatLng)
                    .title("destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                // get direction
                var des = Location("new")
                des.latitude = destinationLatLng.latitude
                des.longitude = destinationLatLng.longitude
                drawPath(mLastLocation, des)
            }
        }
    }

    private fun drawPath(mLastLocation: Location?, location: Location?){
        if (polyLine != null)
            polyLine!!.remove()

        val origin = StringBuilder(mLastLocation!!.latitude.toString())
            .append(",")
            .append(mLastLocation!!.longitude.toString())
            .toString()
        val destination = StringBuilder(location!!.latitude.toString())
            .append(",")
            .append(location!!.longitude.toString())
            .toString()
        Log.i("des", destination)

        mService.getDirections(origin, destination, "AIzaSyC0kPtChwygXsS2m3d6kV9Q-2auyra13ps")
            .enqueue(object:retrofit2.Callback<String>{
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("lalala", t!!.message)
                }

                override fun onResponse(call: Call<String>?, response: Response<String>) {
                    ParserTask().execute(response.body()!!.toString())
                }

            })

    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
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
                ), ViewDirections.MY_PERMISSION_CODE
                )
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), ViewDirections.MY_PERMISSION_CODE
                )
            }
            return false
        } else
            return true
    }

    inner class ParserTask: AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {
        internal val waitingDialog:SpotsDialog = SpotsDialog(this@ViewDirections)

        override fun onPreExecute() {
            super.onPreExecute()
            waitingDialog.show()
            waitingDialog.setMessage("Please Waiting")
        }

        override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>>? {
            val jsonObject: JSONObject
            var routes: List<List<HashMap<String, String>>>?=null
            try{
                jsonObject = JSONObject(params[0])
                val parser = DirectionJSONParser()
                routes = parser.parse(jsonObject)
            }catch (e:JSONException)
            {
                e.printStackTrace()
            }
            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            super.onPostExecute(result)
            var points:ArrayList<LatLng>? = ArrayList<LatLng>()
            var polylineOptions: PolylineOptions? = PolylineOptions()

            for(i in result!!.indices)
            {
                points = ArrayList()
                polylineOptions = PolylineOptions()
                var path = result[i]
                for (j in path.indices){
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                //Log.i("points", points.size().toString())
                polylineOptions.addAll(points)
                polylineOptions.width(13f)
                polylineOptions.color(Color.BLUE)
                polylineOptions.geodesic(true)
            }
            polyLine = mMap!!.addPolyline(polylineOptions)
            waitingDialog.dismiss()
        }
    }
}