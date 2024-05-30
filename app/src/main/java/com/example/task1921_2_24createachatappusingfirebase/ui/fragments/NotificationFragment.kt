package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentNotificationBinding
import com.example.task1921_2_24createachatappusingfirebase.model.NotificationData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.NotificationAdapter
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var listener: ListenerRegistration

    private lateinit var notificationAdapter: NotificationAdapter
    private var notificationListData: ArrayList<NotificationData> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* profile image */
        val userRef = FirebaseDatabase.getInstance().reference
        userRef.child(Constants.REALTIME_DB).child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserData::class.java)
                        Log.d("CHATSUSERDATA", "onDataChange: ${user?.currentUId}")

                        try {
                            Glide.with(requireContext()).load(user?.profileImage)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .apply(RequestOptions().circleCrop())
                                .placeholder(R.drawable.ic_person)
                                .into(binding.ivProfileImg)

                        } catch (e: Exception) {
                            Log.e("Error", "${e.message}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        /*gettingUnreadMessages()*/

        binding.ivProfileImg.setOnClickListener {

        }

        /** back press handling*/
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finishAndRemoveTask()
        }
    }

    /*private fun gettingUnreadMessages() {
        listener = FirebaseFirestore.getInstance().collection("Chats")
            .addSnapshotListener { snapshot, exception ->
//                if (snapshot != null) {
                notificationListData.clear()
                for (doc in snapshot!!.documents) {
                    val data = doc.data
                    val isSeen = data?.get("isSeen") as Boolean
                    val receiver = data["receiver"] as String
//                    val sender = data["sender"] as String
                    val msg = data["message"] as String

//                    Log.e("SENDERTAG", "gettingUnreadMessages: $sender")
                    if (receiver == FirebaseAuth.getInstance().uid && !isSeen) {
                        *//** getting unread messages receiver id *//*
                        notificationListData.add(
                            NotificationData(
                                "",
                                FirebaseAuth.getInstance().uid.toString(),
                                "sender",
                                "",
                                msg,
                                isSeen,
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                            )
                        )
                    }

                    notificationAdapter = NotificationAdapter(notificationListData) { listData ->
                        FirebaseDatabase.getInstance().reference.child("users")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (user in snapshot.children) {
                                        val userData = user.getValue(UserData::class.java)

                                        Log.e("USERDATATAG", "onDataChange: $userData")

                                        val direction =
                                            NotificationFragmentDirections.actionNotificationFragment2ToChatFragment(
                                                UserData(
                                                    listData.message.toString(),
                                                    userData?.profileImage.toString(),
                                                    userData?.userName.toString(),
                                                    userData?.status.toString(),
                                                    userData?.isChat.toString().toBoolean(),
                                                    userData?.emailId.toString()
                                                )
                                            )

                                        findNavController().navigate(direction)

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })

                    }
                    binding.rvContactList.adapter = notificationAdapter
                }
//                }
            }
    }*/
}