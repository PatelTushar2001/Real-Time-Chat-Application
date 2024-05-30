package com.example.task1921_2_24createachatappusingfirebase.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
data class  UserData(
    val currentUId: String? = null,
    val profileImage: String? = null,
    val userName: String? = null,
    val status: String? = null,
    val isChat: Boolean? = null,
    val emailId: String? = null
) : Parcelable
