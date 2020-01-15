package com.aurimteam.recordclient.Models

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.aurimteam.recordclient.Api.Api
import com.aurimteam.recordclient.App
import com.aurimteam.recordclient.IMainModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*


class MainModel : IMainModel {
    private var path = ""

    interface OnFinishedListener {
        fun onResultSuccess(path: String)
        fun onResultFail(strError: String?)
    }

    override fun getImageData(context: Context?, onFinishedListener: OnFinishedListener) {
        App.retrofit
            .create(Api::class.java)
            .getImageFromServer()
            .enqueue(object : Callback<ResponseBody?> {
                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    onFinishedListener.onResultFail("Error")
                }

                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val writtenToDisk: Boolean = writeResponseBodyToDisk(context, responseBody)
                        if (path != "") onFinishedListener.onResultSuccess(path)
                        Log.d(TAG, "file download was a success? $writtenToDisk");
                    } else {
                        Log.d(TAG, "server contact failed");
                        //val jsonObj = JSONObject(response.errorBody()?.string())
                        //onFinishedListener.onResultFail(jsonObj.getJSONObject("error")?.getString("message")?.toString())
                    }
                }
            })
    }

    override fun writeResponseBodyToDisk(context: Context?, body: ResponseBody): Boolean {
        return try {
            val iconFile =
                File(context?.getExternalFilesDir(null).toString() + File.separator.toString() + "screenshot.jpg")
            path = context?.getExternalFilesDir(null).toString() + File.separator.toString() + "screenshot.jpg"
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(iconFile)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) break
                    outputStream?.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream?.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }
}