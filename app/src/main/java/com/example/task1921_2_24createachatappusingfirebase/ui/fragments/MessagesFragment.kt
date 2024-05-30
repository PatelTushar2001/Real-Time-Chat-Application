package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentMessagesBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatListRealData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.MessagesAdapter
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.AppId
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.AppSignIn
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.REALTIME_DB
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.USER_DB
import com.example.task1921_2_24createachatappusingfirebase.utils.removeWhiteSpaces
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutPictureInPictureConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import java.util.Collections

class MessagesFragment : Fragment() {
    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var listener: ListenerRegistration

    private lateinit var messagesAdapter: MessagesAdapter
    private var messagesData: ArrayList<UserData> = arrayListOf()
    private var chatListData: ArrayList<ChatListRealData> = arrayListOf()
    private var uid: ArrayList<String> = arrayListOf()

    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0

    private var currentUserName = ""
    var token: String = ""
    var userUID: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* read firebase data */
        db = FirebaseFirestore.getInstance()

        /* firebase authentication */
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference


        /* getting chat list data */
        FirebaseDatabase.getInstance().reference.child("ChatList")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatListData.clear()
                    for (data in snapshot.children) {
                        val chatId = data.getValue(ChatListRealData::class.java)
                        chatListData.add(ChatListRealData(chatId?.id))
                    }
                    /* updating ui */
                    updateUI()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        listener = db.collection("Chats").addSnapshotListener { snapshot, exception ->
            if (snapshot != null) {
                var countUnseenMsg = 0
                for (doc in snapshot.documents) {
                    val data = doc.data
                    val isSeen = data?.get("isSeen") as Boolean
                    val receiver = data["receiver"] as String

                    if (receiver == FirebaseAuth.getInstance().uid && !isSeen) {
                        countUnseenMsg += 1
                    }

                }

                if (countUnseenMsg == 0) {
                    binding.btnChat.text = "Chats"
                } else {
                    binding.btnChat.text = "(${countUnseenMsg})Chats"
                }
            }
        }

        val userRef = FirebaseDatabase.getInstance().reference

        userRef.child(REALTIME_DB).child(FirebaseAuth.getInstance().uid!!)
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

                            startVC(user?.userName.toString().removeWhiteSpaces())

                        } catch (e: Exception) {
                            Log.e("Error", "${e.message}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        binding.apply {
            ivProfileImg.setOnClickListener {
//                findNavController().navigate(R.id.action_chatFragment_to_mapFragment)
            }

            btnGroups.setOnClickListener {
                findNavController().navigate(R.id.action_messagesFragment_to_groupFragment)
            }
        }

        // back press handling
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finishAndRemoveTask()
        }

        /* get token for every new user */
        generateToken()


        /** back press handling*/
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finishAndRemoveTask()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallInvitationService.unInit()
    }

    /* generating new token */
    private fun generateToken() {
        FirebaseInstallations.getInstance().id.addOnCompleteListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { getToken ->
                token = getToken
                val hashMap = hashMapOf<String, Any>("token" to token)

                FirebaseFirestore.getInstance().collection("Tokens")
                    .document(FirebaseAuth.getInstance().uid!!).set(hashMap)
            }
        }
    }

    private fun updateUI() {
        val ref = FirebaseDatabase.getInstance().reference

        /* list users */
        listener = db.collection(USER_DB).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("Error", "error getting data...")
            }

            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val data = doc.data
                    val userId = data?.get("UserID") as String
                    val userName = data["UserName"] as String

                    if (userId != FirebaseAuth.getInstance().uid) {
                        uid.add(userId)
                    }
                    if (userId == FirebaseAuth.getInstance().uid) {
                        currentUserName = userName
                    }
                }

                uid.forEach {
                    Log.e("STATUSVALUE", it)

                    if (it != FirebaseAuth.getInstance().uid) {
                        ref.child("users").addValueEventListener(object : ValueEventListener {
                            @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
                            override fun onDataChange(snapshot: DataSnapshot) {

                                messagesData.clear()
                                for (data in snapshot.children) {
                                    val user = data.getValue(UserData::class.java)

                                    if (user?.currentUId != FirebaseAuth.getInstance().uid) {
                                        for (value in chatListData) {
                                            if (user?.currentUId == value.id) {
                                                messagesData.add(
                                                    UserData(
                                                        user?.currentUId,
                                                        user?.profileImage,
                                                        user?.userName,
                                                        user?.status,
                                                        user?.isChat,
                                                        user?.emailId
                                                    )
                                                )
                                            }
                                        }

                                        Log.e("STATUSVALUE2", "${messagesData}")
                                    }
                                }
                                messagesAdapter = MessagesAdapter(messagesData, {
                                    val sharedCustomePref = activity?.getSharedPreferences(
                                        "receiver_data",
                                        FirebaseMessagingService.MODE_PRIVATE
                                    )
                                    sharedCustomePref?.edit()
                                        ?.putString("receiverId", it.currentUId!!)?.apply()

                                    Log.d("CurrentUid", "updateUI: ${it.currentUId}")

                                    userUID = it.currentUId!!

                                    /* navigating data to chat fragment */
                                    val direction =
                                        MessagesFragmentDirections.actionMessagesFragmentToChatFragment(
                                            UserData(
                                                it.currentUId.toString(),
                                                it.profileImage.toString(),
                                                it.userName.toString(),
                                                it.status.toString(),
                                                it.isChat,
                                                it.emailId.toString()
                                            )
                                        )
                                    findNavController().navigate(direction)

                                    Log.e("STATUSVALUE", "$it")
                                }, { userData, profileImg ->
                                    //zoomImageFromThumb(profileImg, userData.profileImage!!)
                                    openPopUp(userData)
//                                    startVC(currentUserName.removeWhiteSpaces())

                                })

                                binding.rvMessages.adapter = messagesAdapter
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            }
        }
    }

    /**/
    private fun startVC(uid: String) {
        val appId: Long = AppId.toLong()
        val appSign = AppSignIn
        val userId = uid
        val userName = uid
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        val pipConfig = ZegoLayoutPictureInPictureConfig()
        pipConfig.isSmallViewDraggable = true
        pipConfig.switchLargeOrSmallViewByClick = true
        ZegoUIKitPrebuiltCallInvitationService.init(
            requireActivity().application,
            appId,
            appSign,
            userId,
            userName,
            callInvitationConfig
        )
    }

    private fun openPopUp(userData: UserData) {
        val builder = Dialog(requireContext(), R.style.Theme_BottomSheetTheme)
        val customView =
            LayoutInflater.from(context).inflate(R.layout.custom_view_profile_dialog, null)

        val userName = customView.findViewById<TextView>(R.id.tv_UserName)
        val profileImage = customView.findViewById<ImageView>(R.id.iv_ProfileImage)
        val chatImage = customView.findViewById<ImageView>(R.id.iv_Chat)
        val callImage = customView.findViewById<ZegoSendCallInvitationButton>(R.id.iv_Call)
        val videoImage = customView.findViewById<ZegoSendCallInvitationButton>(R.id.iv_Video)
        val infoImage = customView.findViewById<ImageView>(R.id.iv_Info)
        val mainView = customView.findViewById<ConstraintLayout>(R.id.cly_ViewProfileImg)

        /* profile view main view click event */
        mainView.setOnClickListener {
            builder.dismiss()
        }

        /* profile image click event */
        profileImage.setOnClickListener {
            val action = MessagesFragmentDirections.actionMessagesFragmentToViewFullImageFragment(
                userData.profileImage.toString(),
                true,
                userData.userName.toString()
            )
            findNavController().navigate(action)
            builder.dismiss()
        }

        /* chat ic click event */
        chatImage.setOnClickListener {
            /* navigating data to chat fragment */
            val direction = MessagesFragmentDirections.actionMessagesFragmentToChatFragment(
                UserData(
                    userData.currentUId.toString(),
                    userData.profileImage.toString(),
                    userData.userName.toString(),
                    userData.status.toString(),
                    userData.isChat,
                    userData.emailId.toString()
                )
            )
            findNavController().navigate(direction)
            builder.dismiss()
        }

        /* voice call icon click event */
        setVoiceCall(userData.userName.toString().removeWhiteSpaces(), callImage, builder)
        /* video call icon click event */
        setVideoCall(userData.userName.toString().removeWhiteSpaces(), videoImage, builder)

        /* info ic click event */
        infoImage.setOnClickListener {
            val action = MessagesFragmentDirections.actionMessagesFragmentToProfileFragment(
                userData
            )
            findNavController().navigate(action)
            builder.dismiss()
        }

        /* profile image */
        Glide.with(this).load(userData.profileImage).fitCenter().placeholder(R.drawable.ic_person)
            .override(700, 700).into(profileImage)

        /* username */
        userName.text = userData.userName

        builder.setCancelable(true)
        builder.setContentView(customView)
        builder.show()
    }

    /* start video call fun */
    private fun setVideoCall(uid: String?, ivVideo: ZegoSendCallInvitationButton, dialog: Dialog) {
        dialog.dismiss()
        ivVideo.setIsVideoCall(true)
        ivVideo.resourceID = "zego_uikit_call"
        ivVideo.setInvitees(Collections.singletonList(ZegoUIKitUser(uid)))
    }

    /* start voice call fun */
    private fun setVoiceCall(uid: String?, ivCall: ZegoSendCallInvitationButton, dialog: Dialog) {
        dialog.dismiss()
        ivCall.setIsVideoCall(false)
        ivCall.resourceID = "zego_uikit_call"
        ivCall.setInvitees(Collections.singletonList(ZegoUIKitUser(uid)))
    }
}