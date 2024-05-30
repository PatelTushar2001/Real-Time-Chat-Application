package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawMessageCellBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessagesAdapter(
    private val messagesData: ArrayList<UserData>,
    private val onClick: (UserData) -> Unit,
    private val onProfileClick: (UserData, ImageView) -> Unit,
) : RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>() {

    inner class MessagesViewHolder(val binding: RawMessageCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val view = RawMessageCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessagesViewHolder(view)
    }

    override fun getItemCount(): Int = messagesData.size

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        with(holder) {
            with(binding) {
                Glide.with(itemView.context).load(messagesData[position].profileImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions().circleCrop())
                    .placeholder(R.drawable.ic_person)
                    .into(ivProfileImg)
                tvUserName.text = messagesData[position].userName

                if (messagesData[position].status == "online") {
                    ivCurrentStatus.imageTintList =
                        ContextCompat.getColorStateList(itemView.context, R.color.btn_yellow)
                    ivCurrentStatus.setImageResource(R.drawable.ic_current_status)
                } else {
                    ivCurrentStatus.imageTintList =
                        ContextCompat.getColorStateList(
                            itemView.context,
                            R.color.app_background
                        )
                    ivCurrentStatus.setImageResource(R.drawable.ic_current_status)
                }
                retriveLastMsg(messagesData[position].currentUId, tvLastMsg, tvLastOnline)

                itemView.setOnClickListener {
                    onClick(messagesData[position])
                }

                ivProfileImg.setOnClickListener {
                    // open profile image
                    onProfileClick(messagesData[position], ivProfileImg)
                }
            }
        }
    }

    private fun retriveLastMsg(chatUserId: String?, lastMsg: TextView, msgTime: TextView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refChat = FirebaseDatabase.getInstance().reference.child("Chats")

        refChat.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val chats = data.getValue(ChatData::class.java)

                        if (firebaseUser != null && chats != null) {
                            if (chats.receiver == firebaseUser.uid && chats.sender == chatUserId || chats.receiver == chatUserId && chats.sender == firebaseUser.uid) {

                                if (chats.message.equals("sent you an image.")) {
                                    lastMsg.text = "image"
                                    msgTime.text = chats.timeStamp!!
                                } else {
                                    lastMsg.text = chats.message!!
                                    msgTime.text = chats.timeStamp!!
                                }

                                Log.d("CHATDATA2", "onDataChange: ${chats.isSeen}")
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}