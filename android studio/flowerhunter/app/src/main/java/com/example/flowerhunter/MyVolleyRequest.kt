package com.example.flowerhunter

import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache


import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest

class MyVolleyRequest {
    private var mRequestQueue: RequestQueue?=null
    private var context: Context?=null
    private var iVolley:IVolley?=null
    var imageLoader: ImageLoader?=null
        private set

    val requestQueue:RequestQueue
        get() {
            if(mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(context!!.applicationContext)
            }
            return mRequestQueue!!
        }

    private constructor(context: Context,iVolley: IVolley) {
        this.context=context
        this.iVolley=iVolley
        mRequestQueue=requestQueue
        this.imageLoader= ImageLoader(mRequestQueue,object:ImageLoader.ImageCache{
            private val mCache = LruCache<String, Bitmap>(10)
            override fun getBitmap(url: String): Bitmap? {
                return mCache.get(url)
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                mCache.put(url,bitmap)
            }
        })
    }

    private constructor(context: Context) {
        this.context=context
        mRequestQueue=requestQueue
        this.imageLoader= ImageLoader(mRequestQueue,object:ImageLoader.ImageCache{
            private val mCache = LruCache<String, Bitmap>(10)
            override fun getBitmap(url: String): Bitmap? {
                return mCache.get(url)
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                mCache.put(url,bitmap)
            }
        })
    }

    fun <T> addToRequestQueue(req:Request<T>) {
        requestQueue.add(req)
    }

    fun getRequest(url:String) {
        val getRequest = JsonObjectRequest(Request.Method.GET,url,null, Response.Listener {response ->
            iVolley!!.onResponse(response.toString())
        }, Response.ErrorListener { error->
            iVolley!!.onResponse(error.message!!)
        })

        addToRequestQueue(getRequest)
    }

    companion object {

        private var mInstance:MyVolleyRequest?=null

        @Synchronized
        fun getInstance(context: Context): MyVolleyRequest {
            if(mInstance==null) {
                mInstance = MyVolleyRequest(context)
            }
            return mInstance!!
        }


        @Synchronized
        fun getInstance(context: Context,iVolley: IVolley): MyVolleyRequest {
            if(mInstance==null) {
                mInstance = MyVolleyRequest(context,iVolley)
            }
            return mInstance!!
        }

    }

}