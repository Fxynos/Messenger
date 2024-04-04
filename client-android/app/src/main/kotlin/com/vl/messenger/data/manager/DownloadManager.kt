package com.vl.messenger.data.manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.vl.messenger.ApiException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.InputStream

class DownloadManager(retrofit: Retrofit) {

    private val api = retrofit.create(Api::class.java)

    private fun download(url: String): InputStream = api.download(url).execute().run {
        if (!isSuccessful)
            throw ApiException(this)
        body()!!.byteStream()
    }

    fun downloadBitmap(url: String): Bitmap = BitmapFactory.decodeStream(download(url))

    private interface Api {
        @Streaming
        @GET
        fun download(@Url url: String): Call<ResponseBody>
    }
}