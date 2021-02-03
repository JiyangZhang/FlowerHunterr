package com.example.flowerhunter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import java.util.concurrent.atomic.AtomicBoolean
import java.net.*;
import org.json.JSONObject

class Registration : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }

    /* Function that registers a new user */
    fun register(view: View){

        var username = findViewById<EditText>(R.id.username).text
        var email = findViewById<EditText>(R.id.email).text
        var password = findViewById<EditText>(R.id.password).text


        var doneFlag = AtomicBoolean(false)
        var response = ""

        object : Thread(){
            override fun run() {
                response = URL("https://phase3-257520.appspot.com/user_exists_" + username).readText()
                doneFlag.set((true))
            }
        }.start()

        while(!doneFlag.get()){}

        // unique user
        if(response.equals("null\n")){
            object : Thread(){
                override fun run() {
                    response = URL("https://phase3-257520.appspot.com/register_user_" + username + "_" + email + "_" + password).readText()
                    doneFlag.set((true))
                }
            }.start()
            startActivity(Intent(this, MainActivity::class.java))
            val myToast = Toast.makeText(this, "Registration successful. Please login.", Toast.LENGTH_SHORT)
            myToast.show()


        }
        else{
            val myToast = Toast.makeText(this, "username already exists", Toast.LENGTH_SHORT)
            myToast.show()
        }

    }


}
