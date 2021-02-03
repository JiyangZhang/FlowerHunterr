package com.example.flowerhunter
// Java code from http://android-er.blogspot.com/2015/12/open-image-free-draw-something-on.html referenced
// for help with drawing on ImageView functionality

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Environment
import java.io.File
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.camera.imageView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.drawToBitmap
import kotlinx.android.synthetic.main.activity_create_theme.*
import java.io.FileInputStream
import java.net.*
import java.io.ByteArrayOutputStream
import com.example.flowerhunter.IVolley
import java.lang.Exception
import java.util.*

class CreateTheme : AppCompatActivity(), IVolley {

    private var themeName: EditText? = null
    private var themeDescription: EditText? = null
    private var saveButton: Button? = null
    lateinit var prvX : Integer
    lateinit var prvY : Integer
    lateinit var paintDraw : Paint
    lateinit var canvasMaster : Canvas
    lateinit var bitmapMaster : Bitmap
    lateinit var bitmapOriginal : Bitmap

    companion object {
        var bitmapSet = false
    }



    var picFileName = "test.png"
    var dir = Environment.getExternalStorageDirectory()
    var picLocation = File(dir, picFileName)
    private val url: String = "https://phase3-257520.appspot.com/create_theme"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_theme)

        //Linking variables to user input boxes
        themeDescription = findViewById(R.id.themedescription) as EditText
        themeName = findViewById(R.id.themename) as EditText

        //Displaying test.png in local directory that the user took with the camera
        imageView.setImageResource(R.drawable.flower)

        Picasso
            .get()
            .load(picLocation)
            .into(imageView)



        btnTakePicture.setOnClickListener {
            startActivity(Intent(this, Camera::class.java))
        }

        btnOk.setOnClickListener {
            val username = MainActivity.username
            val theme_title = themeName!!.text.toString()
            val theme_description = themeDescription!!.text.toString()
            //val test = BitmapFactory.decodeStream(FileInputStream(picLocation))
            val test = imageView.drawToBitmap()
            val stream = ByteArrayOutputStream()
            test.compress(Bitmap.CompressFormat.PNG, 50, stream)
            val bytes = Base64.getEncoder().encodeToString(stream.toByteArray())
            // val name: String? = preference.getname()
            MyVolleyRequest2.getInstance(this@CreateTheme, this@CreateTheme)
                .postRequest(url, username, theme_title, theme_description, bytes)

            //Save operation complete, return to home screen
            //startActivity(Intent(this, Home::class.java))

            ///Inform the user the upload completed
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Home::class.java))
            picLocation.delete()

        }

        btnCancel.setOnClickListener {
            try {
                if(bitmapSet){
                    imageView.invalidate()
                    imageView.setImageBitmap(bitmapOriginal)
                }

            }
            catch (e : Exception){
                var blank = imageView.drawToBitmap()
                imageView.setImageBitmap(Bitmap.createBitmap(blank.width,blank.height,blank.config))
            }

        }

        val ac = applicationContext

        // Change brightness amount upon changing brightness slider level
        brightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var bm = imageView.drawToBitmap()

                var cmb = ColorMatrix()

                var contrastVal = 1 - contrast.progress.toFloat()/100.0.toFloat()
                var brightnessVal = i.toFloat()

                cmb.set(floatArrayOf(
                    contrastVal, 0f, 0f, 0f, brightnessVal,
                    0f, contrastVal, 0f, 0f, brightnessVal,
                    0f, 0f, contrastVal, 0f, brightnessVal,
                    0f, 0f, 0f, contrastVal, 0f
                ))

                var colorMatrix = ColorMatrix()
                colorMatrix.set(cmb)

                imageView.setColorFilter(ColorMatrixColorFilter(colorMatrix))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

        })

        // Change contrast amount upon changing contrast slider level
        contrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var bm = imageView.drawToBitmap()

                var cmb = ColorMatrix()

                var contrastVal = 1 - i.toFloat()/100.0.toFloat()
                var brightnessVal = brightness.progress.toFloat()

                cmb.set(floatArrayOf(
                    contrastVal, 0f, 0f, 0f, brightnessVal,
                    0f, contrastVal, 0f, 0f, brightnessVal,
                    0f, 0f, contrastVal, 0f, brightnessVal,
                    0f, 0f, 0f, contrastVal, 0f
                ))

                var colorMatrix = ColorMatrix()
                colorMatrix.set(cmb)

                imageView.setColorFilter(ColorMatrixColorFilter(colorMatrix))

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

        })



        paintDraw = Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setColor(Color.BLACK);
        paintDraw.setStrokeWidth(10f);


        // Draw lines upon touching the imageView pic
        imageView.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(v : View,event : MotionEvent) : Boolean {


                if(!bitmapSet){
                    bitmapOriginal = imageView.drawToBitmap()
                    bitmapSet = true
                 }


                var action = event.getAction()
                var x = Integer(event.x.toInt())
                var y =  Integer(event.y.toInt())

                var bm = imageView.drawToBitmap()

                bitmapMaster = Bitmap.createBitmap(bm)


                if(action == MotionEvent.ACTION_DOWN){
                    prvX = x
                    prvY = y
                    drawOnProjectedBitMap(v as ImageView, bitmapMaster, prvX.toFloat(), prvY.toFloat(), x.toFloat(), y.toFloat())
                }

                else if(action == MotionEvent.ACTION_MOVE){
                    drawOnProjectedBitMap(v as ImageView, bitmapMaster, prvX.toFloat(), prvY.toFloat(), x.toFloat(), y.toFloat())
                    prvX = x
                    prvY = y
                }

                else if(action == MotionEvent.ACTION_UP){
                    drawOnProjectedBitMap(v as ImageView, bitmapMaster, prvX.toFloat(), prvY.toFloat(), x.toFloat(), y.toFloat())


                }

                return true
            }

        })


        val spinner = findViewById<Spinner>(R.id.spinner)
        val colorList = ArrayList<String>()
        colorList.add("Black")
        colorList.add("White")
        colorList.add("Red")
        colorList.add("Green")
        colorList.add("Blue")
        colorList.add("Yellow")

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, colorList)
        spinner.setAdapter(adapter)



        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            // Change paint color on spinner select
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedColor = colorList.get(position)
                if(selectedColor.equals("Black")){
                    paintDraw.setColor(Color.BLACK)
                }
                else if(selectedColor.equals("White")){
                    paintDraw.setColor(Color.WHITE)
                }
                else if(selectedColor.equals("Red")){
                    paintDraw.setColor(Color.RED)
                }
                else if(selectedColor.equals("Green")){
                    paintDraw.setColor(Color.GREEN)
                }
                else if(selectedColor.equals("Blue")){
                    paintDraw.setColor(Color.BLUE)
                }
                else if(selectedColor.equals("Yellow")){
                    paintDraw.setColor(Color.YELLOW)
                }

            }

        }

    }

    // Update imageView with the modified bitmap
    fun drawOnProjectedBitMap(iv : ImageView, bm : Bitmap,
    x0 : Float, y0 : Float, x : Float, y : Float){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return;
        }else{

            var ratioWidth = bm.getWidth().toFloat()/iv.getWidth().toFloat();
            var ratioHeight = bm.getHeight().toFloat()/iv.getHeight().toFloat();

            canvasMaster = Canvas(bitmapMaster)
            canvasMaster.drawBitmap(bitmapMaster, 0f, 0f, null)


            canvasMaster.drawLine(
                x0 * ratioWidth,
                y0 * ratioHeight,
                x * ratioWidth,
                y * ratioHeight,
                paintDraw)
            imageView.invalidate()

            imageView.setImageBitmap(bitmapMaster)

        }
    }









    override fun onResponse(response: String) {
        //Toast.makeText(this@CreateTheme, "" + response, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        bitmapSet = false
    }
}


