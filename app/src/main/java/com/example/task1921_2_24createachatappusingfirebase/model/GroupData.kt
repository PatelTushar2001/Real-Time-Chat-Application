package com.example.task1921_2_24createachatappusingfirebase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupData(
    var userName: String,
    var userImage: String,
    var userId: String
) : Parcelable
