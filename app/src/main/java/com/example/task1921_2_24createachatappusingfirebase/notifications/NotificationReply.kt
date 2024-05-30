package com.example.task1921_2_24createachatappusingfirebase.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.task1921_2_24createachatappusingfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import java.text.SimpleDateFormat
import java.util.Locale


private const val CHANNEL_ID = "my_channel"

class NotificationReply : BroadcastReceiver() {
    val firebase = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val remoteInput = RemoteInput.getResultsFromIntent(intent!!)

        if (remoteInput != null) {
            val sharedCustomePref = context.getSharedPreferences(
                "receiver_data",
                FirebaseMessagingService.MODE_PRIVATE
            )

            val ref = FirebaseDatabase.getInstance().reference
            val msgKey = ref.push().key

            val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
            val currentDate = sdf.format(java.util.Date())

            val receiverId = sharedCustomePref.getString("receiverId", "")
            val repliedText = remoteInput.getString("KEY_REP_TEXT")

            /* add reply text into realtime "Chats" database */
            val msgHashMap = hashMapOf<String, Any>(
                "sender" to FirebaseAuth.getInstance().uid!!,
                "message" to repliedText!!,
                "receiver" to receiverId!!,
                "timeStamp" to currentDate,
                "messageId" to msgKey!!,
            )
            ref.child("Chats")
                .child(msgKey)
                .setValue(msgHashMap)

            /* add reply text into firebase "Chats" firestore */
            val chatHashMap = hashMapOf<String, Any>(
                "receiver" to receiverId,
                "isSeen" to false,
                "timeStamp" to currentDate,
            )
            FirebaseFirestore.getInstance().collection("Chats").document(msgKey).set(chatHashMap)

            /* getting reply-id from sharedPreference */
            val sharedCostomePref = context.getSharedPreferences(
                "my_pref",
                FirebaseMessagingService.MODE_PRIVATE
            )
            val replyid = sharedCostomePref.getInt("value", 0)

            /* updating notification content after replying notification chat */
            val repliedNotification =
                NotificationCompat
                    .Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentText("Reply Sent").build()

            notificationManager.notify(replyid, repliedNotification)

        }
    }
}