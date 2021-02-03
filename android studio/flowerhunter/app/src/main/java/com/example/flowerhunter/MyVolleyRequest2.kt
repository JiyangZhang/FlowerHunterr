package com.example.flowerhunter
import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MyVolleyRequest2 {
    private var mRequestQueue: RequestQueue? = null
    private var context: Context? = null
    private var iVolley: IVolley? = null
    var imageLoader: ImageLoader?=null

    val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null)
                mRequestQueue = Volley.newRequestQueue(context!!.applicationContext)
            return mRequestQueue!!
        }

    private constructor(context: Context, iVolley: IVolley) {
        this.context = context
        this.iVolley = iVolley
        mRequestQueue = requestQueue
        this.imageLoader = ImageLoader(mRequestQueue, object:ImageLoader.ImageCache{
            private val mCache = LruCache<String, Bitmap>(10)
            override fun getBitmap(url: String?): Bitmap? {
                return mCache.get(url!!)
            }

            override fun putBitmap(url: String?, bitmap: Bitmap?) {
                mCache.put(url!!, bitmap!!)
            }
        })
    }

    private constructor(context: Context) {
        this.context = context
        mRequestQueue = requestQueue
        this.imageLoader = ImageLoader(mRequestQueue, object:ImageLoader.ImageCache{
            private val mCache = LruCache<String, Bitmap>(10)
            override fun getBitmap(url: String?): Bitmap? {
                return mCache.get(url!!)
            }

            override fun putBitmap(url: String?, bitmap: Bitmap?) {
                mCache.put(url!!, bitmap!!)
            }
        })
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    // Get method
    fun getRequest(url: String){
        val getRequest = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener { response ->
            iVolley!!.onResponse(response.toString())
        }, Response.ErrorListener { error ->
            iVolley!!.onResponse(error.message!!)
        })
        addToRequestQueue(getRequest)
    }

    // Post method
    fun postRequest(url: String, username: String, theme_title: String,
                    theme_description: String, bytes: String) {
        val params = HashMap<String, String>()
        //params["name"] = name
        params["username"] = username
        params["theme_title"] = theme_title
        params["theme_description"] = theme_description
        params["data"] = bytes
         //params["location"] = aux
        val postRequest = MyStringRequest(params, Request.Method.POST, url,
            Response.Listener { response ->
                iVolley!!.onResponse(response.toString())
            }, Response.ErrorListener { //error ->
                //iVolley!!.onResponse(error.message!!)
            })
        addToRequestQueue(postRequest)
    }

    companion object {
        private var mInstance : MyVolleyRequest2? = null
        @Synchronized
        fun getInstance(context: Context): MyVolleyRequest2 {
            if (mInstance == null) {
                mInstance = MyVolleyRequest2(context)
            }
            return mInstance!!
        }

        @Synchronized
        fun getInstance(context: Context, iVolley: IVolley): MyVolleyRequest2 {
            if (mInstance == null) {
                mInstance = MyVolleyRequest2(context, iVolley)
            }
            return mInstance!!

        }
    }

}