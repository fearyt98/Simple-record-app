package com.aurimteam.recordclient

import android.content.Context
import com.aurimteam.recordclient.Models.MainModel
import okhttp3.ResponseBody

interface IMainModel {
    fun getImageData(context: Context?, onFinishedListener: MainModel.OnFinishedListener)
    fun writeResponseBodyToDisk(context: Context?, body: ResponseBody): Boolean
}