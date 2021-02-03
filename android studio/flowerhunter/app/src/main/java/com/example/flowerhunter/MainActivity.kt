package com.example.flowerhunter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.json.JSONArray
import java.net.*;
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean


class MainActivity : AppCompatActivity() {

    companion object {
        var username = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun register(view: View){
        startActivity(Intent(this, Registration::class.java))
    }

    fun login(view: View){

        var username = findViewById<EditText>(R.id.username).text
        var password = findViewById<EditText>(R.id.password).text

        var doneFlag = AtomicBoolean(false)
        var response = ""

        object : Thread(){
            override fun run() {
                response = URL("https://phase3-257520.appspot.com/find_user_" + username + "_" + password).readText()
                doneFlag.set((true))
            }
        }.start()

        while(!doneFlag.get()){}


        // user found
        if(!response.equals("null\n")){
            val jsonArray = JSONObject(response)
            MainActivity.username = jsonArray.getString("username")
            startActivity(Intent(this, Home::class.java))
        }
        // user not found
        else{
            val myToast = Toast.makeText(this, "username/password incorrect. Please try again.", Toast.LENGTH_SHORT)
            myToast.show()
        }




    }
}
