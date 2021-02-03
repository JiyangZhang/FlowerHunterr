package com.example.flowerhunter

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.drm.DrmStore
import android.graphics.*
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home.*
import android.graphics.drawable.Drawable
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.InputStream
import java.net.*;
import java.io.*;
import org.json.JSONObject
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.AsyncTask
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.os.Build
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import org.json.JSONArray;
import android.content.pm.PackageManager
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import kotlinx.android.synthetic.main.activity_create_theme.*
import org.w3c.dom.Text
import java.util.concurrent.atomic.AtomicBoolean
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat


class Home : AppCompatActivity() {
    companion object {
        var reports = ArrayList<Report>()
    }
    private var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private var longitude: String? = null
    private var latitude: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // A "Spinner" is the dropdown UI selector that will hold the themes
        val themes = findViewById<Spinner>(R.id.themes)

        var doneFlag = AtomicBoolean(false)
        var apiThemes = ""
        var apiReports = ""

       // Get json data for themes
       object : Thread(){
            override fun run() {
                apiThemes = URL("https://phase3-257520.appspot.com/view_all_themes").readText()
                apiReports = URL("https://phase3-257520.appspot.com/view_all_reports").readText()
                doneFlag.set(true)
            }
        }.start()


        // Wait for data to be retrieved
        while(!doneFlag.get()){}
        val themeList = ArrayList<String>()
        var jsonArray = JSONArray(apiThemes)

        // Parse theme titles from json array
        themeList.add("Any Theme")
        for(i in 0..jsonArray.length()-1){
            themeList.add(jsonArray.getJSONObject(i).getString("title"))
        }

        // Create an ArrayAdapter using themesList
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, themeList)

        // Store themes in Spinner
        themes.setAdapter(adapter)


        val menuItems = ArrayList<String>()
        menuItems.add("Menu")
        menuItems.add("Create Report")
        menuItems.add("Create Theme")
        menuItems.add("Nearby Reports")
        menuItems.add("Logout")

        findViewById<Spinner>(R.id.menu).setAdapter(ArrayAdapter<String>(this, R.layout.spinner_item, menuItems))


        val ct = this

        findViewById<Spinner>(R.id.menu).onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedText = (view as TextView).text.toString()
                if(selectedText.equals("Logout")){
                    findViewById<Spinner>(R.id.menu).setSelection(0)
                    startActivity(Intent(ct, MainActivity::class.java))
                }
                else if(selectedText.equals("Nearby Reports")){
                    findViewById<Spinner>(R.id.menu).setSelection(0)
                    startActivity(Intent(ct, MapsActivity::class.java))
                }
                else if(selectedText.equals("Create Theme")){
                    findViewById<Spinner>(R.id.menu).setSelection(0)
                    startActivity(Intent(ct, CreateTheme::class.java))
                }
                else if(selectedText.equals("Create Report")){
                    findViewById<Spinner>(R.id.menu).setSelection(0)
                    startActivity(Intent(ct, Report1::class.java))

                }

            }

        }


        doneFlag.set(false)

        val glideHelpers = ArrayList<RequestBuilder<Drawable>>()
        val nameToPic = HashMap<String,RequestBuilder<Drawable>>()
        // Get pictures
        object : Thread(){
            override fun run() {
                for(i in 0..themeList.size-1){
                    glideHelpers.add(Glide.with(applicationContext).load("https://flowerhunter.appspot.com/" + themeList.get(i)))
                }


                doneFlag.set(true)
            }
        }.start()


        while(!doneFlag.get()){}

        val root = findViewById<LinearLayout>(R.id.scroll)

        for (i in 0..glideHelpers.size-1){
            nameToPic.put(themeList.get(i), glideHelpers.get(i))
        }

        val reports = ArrayList<Report>()

        jsonArray = JSONArray(apiReports)
        for(i in 0..jsonArray.length()-1){
            val title = jsonArray.getJSONObject(i).getString("title")
            val date = jsonArray.getJSONObject(i).getString("date")
            val description = jsonArray.getJSONObject(i).getString("description")
            val tags = jsonArray.getJSONObject(i).getString("tags")
            val theme = jsonArray.getJSONObject(i).getJSONObject("theme").getString("title")
            val aux = jsonArray.getJSONObject(i).getString("aux")

            var long = ""
            var lat = ""
            if(!aux.equals("")){
                long = aux.split(" ").get(0)
                lat = aux.split(" ").get(1)
            }
            else{
                lat = "0"
                long = "0"
            }

            val pic = ImageView(applicationContext)
            (nameToPic.get(theme) as RequestBuilder<Drawable>).into(pic)

            reports.add(Report(applicationContext, title, date, description, tags, pic, theme, long.toDouble(), lat.toDouble()))
            reports.get(i).addReport(root)
        }

        Home.reports = reports

        root.setPadding(0,0,0,200)


        //Update mileage label upon seek progress change
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                distanceLabel.setText("Distance (" + i.toInt() + " mi)")
                if(i.toInt() == 100){
                    distanceLabel.setText("Distance (inf mi)")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

        })



        //Get current location
//        getLocation()
//        Toast.makeText(this, latitude, Toast.LENGTH_SHORT).show()
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

    }


    public class Report(ac: Context, title: String, date: String, description: String, tags: String, pic: ImageView, theme: String, long: Double, lat: Double) {
        lateinit var title : TextView
        lateinit var date : TextView
        lateinit var description : TextView
        lateinit var tags  : TextView
        lateinit var pic : ImageView
        lateinit var theme: String
        var lat: Double = 0.0
        var long: Double = 0.0
        var show = true

        init{
            this.title = TextView(ac)
            this.title.setTextSize(TypedValue.COMPLEX_UNIT_SP,25f)
            this.title.setTypeface(null, Typeface.BOLD)
            this.title.setTextColor(Color.BLACK)
            this.title.text = title

            this.date = TextView(ac)
            this.date.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f)
            this.date.setTextColor(Color.BLACK)
            this.date.text=date

            this.description = TextView(ac)
            this.description.setTextSize(TypedValue.COMPLEX_UNIT_SP,20f)
            this.description.setTextColor(Color.BLACK)
            this.description.text = description

            this.tags = TextView(ac)
            this.tags.setPadding(0,0,0,100)
            this.tags.setTextColor(Color.RED)
            this.tags.text = "Tags: " + tags

            this.pic = pic

            this.theme = theme

            this.lat = lat
            this.long = long
        }

        fun addReport(ll : LinearLayout){

            ll.addView(title)
            ll.addView(date)
            ll.addView(pic)
            ll.addView(description)
            ll.addView(tags)

        }

    }


    fun search (view: View){
        val tags = findViewById<EditText>(R.id.search_label).text.split(" ")
        val theme = findViewById<Spinner>(R.id.themes).selectedItem as String

        val root = findViewById<LinearLayout>(R.id.scroll)
        root.removeAllViews()

        if( (tags.size == 0 || tags.contains("reports...")) && theme.equals("Any Theme") && seekBar.progress == 100){
            for(i in 0..Home.reports.size-1){
                reports.get(i).addReport(root)
            }
            return
        }

        for(i in 0..Home.reports.size-1){
            Home.reports.get(i).show = false
        }

        // Filter by themes
        if(!theme.equals("Any Theme")){
            for(i in 0..Home.reports.size-1){
                if(Home.reports.get(i).theme.equals(theme)){
                    Home.reports.get(i).show = true
                }
            }
        }
        else{
            for(i in 0..Home.reports.size-1){
                Home.reports.get(i).show = true
            }
        }


        // Filter by tags
        if(!(tags.size == 0 || tags.contains("reports..."))){
            for(i in 0..Home.reports.size-1){
                if(Home.reports.get(i).show){
                    for(j in 0..tags.size-1){
                        val title = Home.reports.get(i).title.text.toString().toLowerCase()
                        val description =  Home.reports.get(i).description.text.toString().toLowerCase()
                        val tagz = Home.reports.get(i).tags.text.toString().toLowerCase()
                        if(title.contains(tags.get(j).toLowerCase()) ||
                            description.contains(tags.get(j).toLowerCase()) ||
                            tagz.contains(tags.get(j).toLowerCase())){

                            Home.reports.get(i).show = true
                            break

                        }
                        else{
                            Home.reports.get(i).show = false
                        }
                    }
                }
            }
        }


        var noneFound = true
        for(i in 0..Home.reports.size-1){
            var d = distance(latitude!!.toDouble(), longitude!!.toDouble(), reports.get(i).lat, reports.get(i).long)
            var lat1 = latitude!!.toDouble()
            var long1 = longitude!!.toDouble()
            var lat2 = reports.get(i).lat
            var long2 =  reports.get(i).long
            if(Home.reports.get(i).show && (distance(latitude!!.toDouble(), longitude!!.toDouble(), reports.get(i).lat, reports.get(i).long) < seekBar.progress || seekBar.progress == 100)){
                Home.reports.get(i).addReport(root)
                noneFound = false
            }
        }

        if(noneFound){
            Toast.makeText(this, "0 search results found. Please broaden your search!", Toast.LENGTH_SHORT).show()
        }


    }

    // new since Glide v4
    @GlideModule
    class MyAppGlideModule : AppGlideModule() {

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
                    //Toast.makeText(this, "Latitude:"+locationGps!!.latitude.toString(), Toast.LENGTH_SHORT).show()
                    //Toast.makeText(this, "Longitude:"+locationGps!!.longitude.toString(), Toast.LENGTH_SHORT).show()
                    latitude = locationGps!!.latitude.toString()
                    longitude = locationGps!!.longitude.toString()
                }
                else{
                    //Toast.makeText(this, "Latitude:"+locationNetwork!!.latitude.toString(), Toast.LENGTH_SHORT).show()
                    //Toast.makeText(this, "Longitude:"+locationNetwork!!.longitude.toString(), Toast.LENGTH_SHORT).show()
                    latitude = locationNetwork!!.latitude.toString()
                    longitude = locationNetwork!!.longitude.toString()
                }
            }

        }

    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double {
        var theta = lon1 - lon2
        var dist = Math.sin((lat1 * Math.PI) / 180.0) * Math.sin((lat2 * Math.PI) / 180.0) + Math.cos((lat1 * Math.PI) / 180.0) * Math.cos((lat2 * Math.PI) / 180.0) * Math.cos((theta * Math.PI) / 180.0)
        dist = Math.acos(dist)
        dist = (dist * 180.0) / Math.PI
        dist = dist * 60 * 1.1515
        dist = dist * 0.8684

        return (dist)
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices){ // bugs
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED){
                allSuccess = false
            }
        }
        return allSuccess
    }

}
