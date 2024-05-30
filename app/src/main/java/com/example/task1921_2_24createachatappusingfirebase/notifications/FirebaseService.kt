package com.example.task1921_2_24createachatappusingfirebase.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.notifications.Constants.KEY_REP_TEXT
import com.example.task1921_2_24createachatappusingfirebase.ui.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val CHANNEL_ID = "my_channel"

class FirebaseService : FirebaseMessagingService() {
    private var listener: ListenerRegistration? = null

    companion object {
        private var sharedPrefs: SharedPreferences? = null

        var token: String?
            get() {
                return sharedPrefs?.getString("token", "")
            }
            set(value) {
                sharedPrefs?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }

    @SuppressLint("NotificationPermission", "ObsoleteSdkInt")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        generateNotification(message)

        val sharedCustomePref = getSharedPreferences(
            "receiver_data",
            MODE_PRIVATE
        )
        val selectedUid = sharedCustomePref.getString("receiverId", "")
//        listener =
//            FirebaseFirestore.getInstance().collection("Tokens")
//                .addSnapshotListener { snapshot, error ->
//                    if (snapshot != null) {
//                        for (doc in snapshot.documents) {
//                            val data = doc.data
//                            val token = data?.get("token") as String
//                            val state = data["state"] as String
//
//                            if (selectedUid?.isNotEmpty() == true && token.isNotEmpty()) {
//                                if (state != "message" && state != "chat") {
//                                    generateNotification(message)
//                                    break
//                                }
//                            }
//                        }
//                    }
//                    if (value != null) {
//                        for (doc in value.d)
//                        val tokenObject = value.toObject(Token::class.java)
//
//                        if (selectedUid.isNotEmpty() && tokenObject?.token!!.isNotEmpty()) {
//                            if (tokenObject.state != "message" && tokenObject.state != "chat") {
//                                generateNotification(message)
//                            } else{
//                                listener?.remove()
//                            }
//                        }
//                    }
//                }
    }

    private fun generateNotification(message: RemoteMessage) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificatioId = kotlin.random.Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        /* intent creation */
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        /* chat notification reply */
        val remoteInput = RemoteInput.Builder(KEY_REP_TEXT)
            .setLabel("Reply")
            .build()
        val replyIntent = Intent(this, NotificationReply::class.java)

        val replyPendingIntent =
            PendingIntent.getBroadcast(this, 0, replyIntent, PendingIntent.FLAG_MUTABLE)

        val replyAction =
            NotificationCompat.Action.Builder(R.drawable.ic_send, "Reply", replyPendingIntent)
                .addRemoteInput(remoteInput).build()

        /* storing notification-id in sharedPrefrence */
        val sharedCostomePref = getSharedPreferences("my_pref", MODE_PRIVATE)
        sharedCostomePref.edit().putInt("value", notificatioId).apply()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("${message.data["title"]} : ${message.data["message"]}")
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 0, 1000, 0))
            .addAction(replyAction)
            .setOnlyAlertOnce(true)
            .build()

        try {
            notificationManager.notify(notificatioId, notification)
        } catch (e: Exception) {
            Log.d("Error", "onMessageReceived: ${e.message}")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "My Chat App"
            enableLights(true)
            lightColor = Color.GRAY
        }

        notificationManager.createNotificationChannel(channel)
    }
}