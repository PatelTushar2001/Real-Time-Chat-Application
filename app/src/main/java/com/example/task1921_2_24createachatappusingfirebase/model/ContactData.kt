package com.example.task1921_2_24createachatappusingfirebase.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContactData(
    var name: String? = null,
    var number: String? = null,
    var image: String? = null,
    var sender: String? = null,
) : Parcelable
