package com.example.flowerhunter

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.flowerhunter.IVolley

class MyStringRequest(
    params: HashMap<String, String>,
    method: Int,
    url: String?,
    listener: Response.Listener<String>?,
    errorListener: Response.ErrorListener?
) : StringRequest(method, url, listener, errorListener) {
    var param: MutableMap<String, String> = params
    override fun getParams(): MutableMap<String, String> {
        return param
    }
}

