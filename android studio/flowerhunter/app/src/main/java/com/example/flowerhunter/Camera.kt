package com.example.flowerhunter


import android.Manifest
import kotlinx.android.synthetic.main.camera.*
import kotlin.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.*
import io.fotoapparat.view.CameraView
import java.io.File

//Gabriel Tanner's camera tutorial was very helpful in writing this code
//https://android.jlelse.eu/create-an-android-camera-app-using-kotlin-459543cec5a7
//Used fotoapparat open source camera library to help capture images

class Camera : AppCompatActivity() {

    var fotoapparat: Fotoapparat? = null
    var fileName = "/test.png"
    var sd = Environment.getExternalStorageDirectory()
    var saveLocation =  File(sd, fileName)
    var fotoapparatState : FotoapparatState? = null
    var cameraStatus : CameraState? = null

    val permissions = arrayOf(android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera)

        //Using third part tool fotoapparat to interact with device camera
        createFotoapparat()

        cameraStatus = CameraState.BACK
        fotoapparatState = FotoapparatState.OFF

        //On click listeners for the three buttons
        fab_camera.setOnClickListener {
            print("Taking photo")
            takePhoto()
            CreateTheme.bitmapSet = false
        }

        fab_switch_camera.setOnClickListener {
            switchCamera()
        }

        fab_back.setOnClickListener {
            val intent = Intent(this, CreateTheme::class.java)
            startActivity(intent)
        }
    }

    private fun createFotoapparat(){
        val cameraView = findViewById<CameraView>(R.id.camera_view)

        fotoapparat = Fotoapparat(
            context = this,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back(),
            logger = loggers(
                logcat()
            ),
            cameraErrorCallback = { error ->
                println("Recorder errors: $error")
            }
        )

    }private fun switchCamera() {
        fotoapparat?.switchTo(
            lensPosition =  if (cameraStatus == CameraState.BACK) front() else back(),
            cameraConfiguration = CameraConfiguration()
        )

        if(cameraStatus == CameraState.BACK) cameraStatus = CameraState.FRONT
        else cameraStatus = CameraState.BACK
    }

    val photoResult = fotoapparat?.takePicture()

    private fun takePhoto() {
        if (hasNoPermissions()) {

            val permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }else {
            println("Has all permissions!")


            fotoapparat
                ?.takePicture()
                ?.saveToFile(saveLocation)

            //val x = (saveLocation as File).readBytes()
            //val y = 3

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        println("Onstart")

        if (hasNoPermissions()) {
            requestPermission()
        }else{
            fotoapparat?.start()
            fotoapparatState = FotoapparatState.ON
        }
    }

    //To be able to take and sve the picture we need all permissions else ask again
    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
        FotoapparatState.OFF
    }

    override fun onPause() {
        super.onPause()

        println("OnPause")
    }

    override fun onResume() {
        super.onResume()
        println("OnResume")

        println(fotoapparatState)

        if(!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF){
            val intent = Intent(baseContext, Camera::class.java)
            startActivity(intent)
            finish()
        }
    }
}

enum class CameraState{
    FRONT, BACK
}

enum class FotoapparatState{
    ON, OFF
}




