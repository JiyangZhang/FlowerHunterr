package com.example.flowerhunter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.JsonReader
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_picture.*
import org.json.JSONObject
import org.xml.sax.Parser

class PictureActivity : AppCompatActivity(),IVolley {

    override fun onResponse(response: String) {
        Toast.makeText(this,"222", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        val bundle = intent.extras
       // val theme: String? = bundle!!.getString("theme")
      //  val t = JSONObject(theme.toString())
        val title = bundle!!.getString("title")
        val content = bundle.getString("content")
        val ch = title!!.split(",")
        val lat = bundle.getString("latitude")
        val lng = bundle.getString("longitude")

        image_view.setImageUrl("https://phase3-257520.appspot.com/"+ch[1],
            MyVolleyRequest.getInstance(this).imageLoader)
        btn_ViewDirection.setOnClickListener{
            val viewDirections = Intent(this@PictureActivity, ViewDirections::class.java )
            viewDirections.putExtra("latitude", lat)
            viewDirections.putExtra("longitude", lng)
            startActivity(viewDirections)
        }
        textView2.setText(ch[0])
        textView3.setText(content)

    }


}
