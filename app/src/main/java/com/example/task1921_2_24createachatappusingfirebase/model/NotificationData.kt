package com.example.task1921_2_24createachatappusingfirebase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationData(
    val timeStamp: String? = null,
    val receiver: String? = null,
    val sender: String? = null,
    val messageId: String? = null,
    val message: String? = null,
    var isSeen: Boolean = false,
    var url: String? = null,
    var fileName: String? = null,
    val contact: String? = null,
    val fileExtention: String? = null,
    val type: String? = null,
    val state: String? = null,
) : Parcelable
