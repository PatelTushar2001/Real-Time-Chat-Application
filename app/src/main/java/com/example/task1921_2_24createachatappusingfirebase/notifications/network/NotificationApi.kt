package com.example.task1921_2_24createachatappusingfirebase.notifications.network

import com.example.task1921_2_24createachatappusingfirebase.notifications.Constants.CONTENT_TYPE
import com.example.task1921_2_24createachatappusingfirebase.notifications.Constants.SERVER_KEY
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {
    @Headers("Authorization:key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")

    @POST("fcm/send")
    suspend fun postNotification(@Body notification : PushNotification): Response<ResponseBody>
}