package com.aurimteam.recordclient.Api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers


interface Api {
    @Headers("Accept: image/jpeg")
    @GET("screenshot.jpg")
    fun getImageFromServer(): Call<ResponseBody?>
}