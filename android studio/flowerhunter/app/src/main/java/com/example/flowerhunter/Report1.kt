package com.example.flowerhunter
import android.content.Intent
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.flowerhunter.IVolley
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_report.*
import org.json.JSONArray
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import java.util.jar.Manifest


class  Report1: AppCompatActivity(), IVolley{
    private var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private var ReportTitile: EditText? = null
    private var ReportTags: EditText? = null
    private var ReportDescription: EditText? = null
    private var longitude: String? = null
    private var latitude: String? = null
    private val url: String = "https://flowerhunter.appspot.com"


    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices){ // bugs
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED){
                allSuccess = false
            }
        }
        return allSuccess
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork){
            if(hasGps){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?){
                            if(location!=null){
                                locationGps = location
                            }
                        }
                        override fun onProviderEnabled(provider: String?) {
                        }

                        override fun onProviderDisabled(provider: String?) {
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?)
                        {
                        }

                })
                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null){
                    locationGps = localGpsLocation
                    latitude = locationGps!!.latitude.toString()
                    longitude = locationGps!!.longitude.toString()
                }

            }
            if(hasNetwork){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0F,object: LocationListener{
                    override fun onLocationChanged(location: Location?){
                        if(location!=null){
                            locationNetwork = location
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    }

                    override fun onProviderEnabled(provider: String?) {
                    }

                    override fun onProviderDisabled(provider: String?) {
                    }
                })
                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null){
                    locationNetwork = localNetworkLocation
                    latitude = locationNetwork!!.latitude.toString()
                    longitude = locationNetwork!!.longitude.toString()
                }

            }

            if (locationGps!=null && locationNetwork!=null){
                if(locationGps!!.accuracy>locationNetwork!!.accuracy){
                    Toast.makeText(this, "Latitude:"+locationGps!!.latitude.toString(), Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Longitude:"+locationGps!!.longitude.toString(), Toast.LENGTH_SHORT).show()
                    latitude = locationGps!!.latitude.toString()
                    longitude = locationGps!!.longitude.toString()
                }
                else{
                    Toast.makeText(this, "Latitude:"+locationNetwork!!.latitude.toString(), Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Longitude:"+locationNetwork!!.longitude.toString(), Toast.LENGTH_SHORT).show()
                    latitude = locationNetwork!!.latitude.toString()
                    longitude = locationNetwork!!.longitude.toString()
                }
            }

        }

    }

    /*private fun enableView(){
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
        btn_loc.isEnabled = true
        btn_loc.setOnClickListener{ getLocation() }
    }

    private fun disableView(){
        btn_loc.isEnabled = false
    }*/

    override fun onResponse(response: String) {
        Toast.makeText(this, ""+response, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // getting location part
        //disableView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkPermission(permissions)) {
                getLocation()
                //enableView()
            }
            else{
                ActivityCompat.requestPermissions(this,
                    permissions, 1)
            }
        }
        else{
            getLocation()
            //enableView()
        }

        ReportTitile = findViewById(R.id.ReportTitile)
        ReportTags = findViewById(R.id.ReportTags)
        ReportDescription = findViewById(R.id.ReportDescription)
        val Themes = findViewById<Spinner>(R.id.SpinnerThemes)
        var doneFlag1 = AtomicBoolean(false)
        var apiResponse = ""
        // Get json data for themes
        object : Thread(){
            override fun run() {
                apiResponse = URL("https://phase3-257520.appspot.com/view_all_themes").readText()
                doneFlag1.set(true)
            }
        }.start()

        // Wait for data to be retrieved
        while(!doneFlag1.get()){}
        val themeList = ArrayList<String>()
        val jsonArray = JSONArray(apiResponse)

        // Parse theme titles from json array
        for(i in 0..jsonArray.length()-1){
            themeList.add(jsonArray.getJSONObject(i).getString("title"))
        }
        // Create an ArrayAdapter using themesList
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, themeList)
        // Store themes in Spinner
        Themes.adapter = adapter

        btn_submit.setOnClickListener{
            val username: String = MainActivity.username
            val reTitle: String = ReportTitile!!.text.toString()
            val tags: String = ReportTags!!.text.toString()
            val des: String = ReportDescription!!.text.toString()
            val thTitle: String = Themes!!.getSelectedItem().toString()
            val long: String = longitude!!
            val lat: String = latitude!!
            val aux: String = long + " " + lat
            var doneFlag = AtomicBoolean(false)
            var response = ""
            object : Thread(){
                override fun run() {
                    response = URL("https://phase3-257520.appspot.com/submit_report_"+username+"_"+thTitle+"_"+reTitle+"_"+des+"_"+tags+"_"+aux).readText()
                    doneFlag.set((true))
                }
            }.start()

            while(!doneFlag.get()){}
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Home::class.java))

            /*
            MyVolleyRequest.getInstance(this@MainActivity, this@MainActivity)
                .postRequest(url, thTitle, reTitle, tags, des, long, lat)
            */
        }

        btn_cancel.setOnClickListener{
            startActivity(Intent(this, Home::class.java))
    }
}}
