package com.example.task1921_2_24createachatappusingfirebase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SendChatData(
    val fileExtention: String? = null,
    val fileName: String? = null,
    val timestamp: String? = null,
    val type: String? = null,
    val url: String? = null,
) : Parcelable
