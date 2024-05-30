package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawAdduserCellBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatListRealData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddUserAdapter(
    private val messagesData: ArrayList<UserData>,
    private val onClick: (UserData) -> Unit,
    private val onProfileClick: (UserData, ImageView) -> Unit,
) : RecyclerView.Adapter<AddUserAdapter.MessagesViewHolder>() {
    private var addNewUid: ArrayList<ChatListRealData> = arrayListOf()

    inner class MessagesViewHolder(val binding: RawAdduserCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val view = RawAdduserCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessagesViewHolder(view)
    }

    override fun getItemCount(): Int = messagesData.size

    override fun onBindViewHolder(holder: MessagesViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with(holder) {
            with(binding) {
                // profile image
                Glide.with(itemView.context).load(messagesData[position].profileImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions().circleCrop())
                    .placeholder(R.drawable.ic_person)
                    .into(ivProfileImg)

                // username
                tvUserName.text = messagesData[position].userName

                // default end icon value
                ivAddUser.visibility = View.VISIBLE
                ivOpenChat.visibility = View.GONE

                /* update endIcon based on chatListData */
                addNewUid.clear()
                FirebaseDatabase.getInstance().reference.child("ChatList")
                    .child(FirebaseAuth.getInstance().uid!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                addNewUid.clear()
                                for (data in snapshot.children) {
                                    val chatId = data.getValue(ChatListRealData::class.java)

                                    addNewUid.add(ChatListRealData(chatId?.id))
                                }
                                for (userId in addNewUid) {
                                    when (messagesData[position].currentUId) {
                                        userId.id -> {
                                            ivOpenChat.visibility = View.VISIBLE
                                            ivAddUser.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

                // recyclerview cell click event
                itemView.setOnClickListener {
                    onClick(messagesData[position])
                }

                // add user icon click event
                ivAddUser.setOnClickListener {
                    Toast.makeText(itemView.context, "open Profile", Toast.LENGTH_SHORT).show()

                    val chatListRefHashMap = hashMapOf<String, Any>(
                        "id" to messagesData[position].currentUId!!,
                        "isValidUser" to ""
                    )

                    val chatListReceiverRefHashMap = hashMapOf<String, Any>(
                        "id" to FirebaseAuth.getInstance().uid!!,
                        "isValidUser" to ""
                    )

                    val chatListRef =
                        FirebaseDatabase.getInstance().reference
                            .child("ChatList")
                            .child(FirebaseAuth.getInstance().uid!!)
                            .child(messagesData[position].currentUId!!)

                    chatListRef.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                chatListRef.setValue(chatListRefHashMap)
                            }
                            val chatsReceiverRef =
                                FirebaseDatabase.getInstance().reference
                                    .child("ChatList")
                                    .child(messagesData[position].currentUId!!)
                                    .child(FirebaseAuth.getInstance().uid!!)

                            chatsReceiverRef.setValue(chatListReceiverRefHashMap)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
//                    itemView.findNavController().popBackStack()
                }

                // chat icon click event
                ivOpenChat.setOnClickListener {
                    onClick(messagesData[position])
                }

                ivProfileImg.setOnClickListener {
                    // open profile image
                    onProfileClick(messagesData[position], ivProfileImg)
                }
            }
        }
    }
}