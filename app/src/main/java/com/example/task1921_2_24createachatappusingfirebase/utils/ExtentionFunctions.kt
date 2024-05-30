package com.example.task1921_2_24createachatappusingfirebase.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.task1921_2_24createachatappusingfirebase.model.ChatData
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.REALTIME_DB
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.USER_DB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream



fun checkEmail(string: String): Boolean {
    val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    return emailPattern.matches(string)
}

fun checkPassword(string: String): Boolean {
    val passRegex =
        Regex(
            "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?!.*\\s).{4,}\$"
        )
    return passRegex.matches(string)
}

/* updating user online-offline status */
fun updateStatus(status: String) {
    val ref = FirebaseDatabase.getInstance().reference

    val statusValue =  hashMapOf<String, Any>()
    statusValue["status"] = status

    try {
        ref.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .updateChildren(statusValue)
    } catch (e: Exception) {
        Log.e("STATUSVALUE", "${e.message}")
    }
}

fun updateIsChatValue(uid: String, state: Boolean) {
    val isChatValue =  hashMapOf<String, Any>()
    isChatValue["isChat"] = state

    try {
        FirebaseFirestore.getInstance().collection(USER_DB)
            .document(uid).update(isChatValue)
        FirebaseDatabase.getInstance().reference.child(REALTIME_DB).child(uid).updateChildren(isChatValue)
    } catch (e: Exception) {
        Log.e("STATUSVALUE", "${e.message}")
    }
}

/* get file Extention */
fun getFileExtention(context: Context, uri: Uri): String? {
    val cr: ContentResolver = context.contentResolver
    val mime = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cr.getType(uri))
}

/* convert bitmap value into uri format */
fun convertBitmapToUri(context: Context, imageUri: Bitmap): Uri {
    val bytes = ByteArrayOutputStream()
    imageUri.compress(Bitmap.CompressFormat.JPEG,100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, imageUri, "Title", null)
    return Uri.parse(path)
}

fun String.removeWhiteSpaces() = replace(" ", "")

/* update isseen property of user chats */
private lateinit var seenListener: ValueEventListener

fun changeStateOfMessage(userId: String, state: String) {
    val reference = FirebaseDatabase.getInstance().reference.child("Chats")

    seenListener = reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (data in snapshot.children) {
                val chat = data.getValue(ChatData::class.java)

                Log.d("CHATDATA2", "onDataChange: ${chat?.messageId}")

                if (chat?.receiver == FirebaseAuth.getInstance().uid && chat?.sender == userId) {
                    FirebaseFirestore.getInstance().collection("Chats")
                        .document(chat.messageId!!).update("state", state)
                        .addOnCompleteListener { }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
        }
    })
}

fun checkInternetIsOn(context: Context): Boolean {
    try {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (connectivityManager != null) {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            }
        }
    } catch (e: Exception) {
        // Log or handle the exception accordingly
        e.printStackTrace()
    }
    return false
}