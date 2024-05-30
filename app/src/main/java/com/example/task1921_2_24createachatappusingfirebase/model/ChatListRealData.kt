package com.example.task1921_2_24createachatappusingfirebase.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatListRealData(
    val id: String? = null,
    val isValidUser: String? = null
) : Parcelable