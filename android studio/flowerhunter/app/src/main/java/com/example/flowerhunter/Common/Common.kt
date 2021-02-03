package com.example.flowerhunter.Common

import com.example.flowerhunter.IGoogleAPIService
import com.example.flowerhunter.RetrofitScalarsClient

object Common {

    private val GOOGLE_API_URL = "https://maps.googleapis.com/"

    val googleApiServiceScalars: IGoogleAPIService
        get()=RetrofitScalarsClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
}