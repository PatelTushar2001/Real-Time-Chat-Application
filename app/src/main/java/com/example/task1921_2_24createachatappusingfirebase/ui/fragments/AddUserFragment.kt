package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.annotation.SuppressLint
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
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentAddUserBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatListRealData
import com.example.task1921_2_24createachatappusingfirebase.model.GroupData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.AddUserAdapter
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.GroupAdapter
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AddUserFragment : Fragment() {
    private var _binding: FragmentAddUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var messagesAdapter: AddUserAdapter
    private var messagesData: ArrayList<UserData> = arrayListOf()

    private lateinit var selectedGroupAdapter: GroupAdapter
    private var selectedUserListData: ArrayList<UserData> = arrayListOf()
    private var finalSelectedUserListData: ArrayList<UserData> = arrayListOf()

    private var uid: ArrayList<String> = arrayListOf()
    private var isNewChat: ArrayList<Boolean> = arrayListOf()

    private var chatListData: ArrayList<ChatListRealData> = arrayListOf()
    private lateinit var listener: ListenerRegistration

    private var currentUserName = ""
    var userUID: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* updating ui */
        updateUI()

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

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }

            ivProfileImg.setOnClickListener {
                findNavController().navigate(R.id.action_addUserFragment_to_editFragment)
            }

            btnCreateGroups.setOnClickListener {
                if (btnCreateGroups.text.equals("Save")) {
                    btnCreateGroups.text = "Create Group"
                    rvCreateGroup.visibility = View.GONE
                    etAddGroupPeople.visibility = View.GONE
                    etAddGroupPeople.text!!.clear()
                    selectedUserListData.clear()
                    finalSelectedUserListData.clear()

                    if (etAddGroupPeople.text!!.isNotEmpty()) {
                        createGroup()
                    }

                } else {
                    rvCreateGroup.visibility = View.VISIBLE
                    etAddGroupPeople.visibility = View.VISIBLE
                    btnCreateGroups.text = "Save"
                }
            }
        }

        /** back press handling*/
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finishAndRemoveTask()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun createGroup() {
        finalSelectedUserListData.distinct().forEach {
            val groupHashMap = hashMapOf<String, Any>(
                "user" to it.userName.toString(),
                "image" to it.profileImage.toString(),
                "uid" to it.currentUId.toString()
            )

            FirebaseDatabase.getInstance().reference.child("Groups")
                .child(binding.etAddGroupPeople.text.toString()).child(it.currentUId!!)
                .setValue(groupHashMap)
        }
    }

    private fun updateUI() {
        val ref = FirebaseDatabase.getInstance().reference
        selectedUserListData.clear()
        finalSelectedUserListData.clear()

        /* list users */
        listener = FirebaseFirestore.getInstance().collection(Constants.USER_DB)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("Error", "error getting data...")
                }

                if (snapshot != null) {
                    isNewChat.clear()

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

                    uid.forEach { it ->
                        Log.e("STATUSVALUE", it)

                        if (it != FirebaseAuth.getInstance().uid) {
                            ref.child("users")
                                .addValueEventListener(object : ValueEventListener {
                                    @SuppressLint(
                                        "NotifyDataSetChanged",
                                        "SuspiciousIndentation"
                                    )
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        messagesData.clear()
                                        for (data in snapshot.children) {
                                            val user = data.getValue(UserData::class.java)

                                            if (user?.currentUId != FirebaseAuth.getInstance().uid) {
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
                                                Log.e("STATUSVALUE2", "${messagesData}")
                                            }
                                        }
                                        messagesAdapter = AddUserAdapter(messagesData,
                                            { userData ->
                                                selectedUserListData.add(
                                                    userData
                                                )
                                                selectedUserListData.filter {
                                                    finalSelectedUserListData.add(
                                                        it
                                                    )
                                                }

                                                selectedGroupAdapter =
                                                    GroupAdapter(selectedUserListData)
                                                binding.rvCreateGroup.adapter = selectedGroupAdapter
                                                selectedGroupAdapter.notifyDataSetChanged()
//                                                val sharedCustomePref =
//                                                    activity?.getSharedPreferences(
//                                                        "receiver_data",
//                                                        FirebaseMessagingService.MODE_PRIVATE
//                                                    )
//                                                sharedCustomePref?.edit()
//                                                    ?.putString("receiverId", it.currentUId!!)
//                                                    ?.apply()
//
//                                                Log.d("CurrentUid", "updateUI: ${it.currentUId}")
//
//                                                userUID = it.currentUId!!
//
//                                                /* navigating data to chat fragment */
//                                                val direction =
//                                                    AddUserFragmentDirections.actionAddUserFragmentToChatFragment(
//                                                        UserData(
//                                                            it.currentUId.toString(),
//                                                            it.profileImage.toString(),
//                                                            it.userName.toString(),
//                                                            it.status.toString(),
//                                                            it.isChat,
//                                                            it.emailId.toString()
//                                                        )
//                                                    )
//                                                findNavController().navigate(direction)
//
//                                                Log.e("STATUSVALUE", "$it")
                                            },
                                            { userData, profileImg ->
                                                // profile image click event
//                                                zoomImageFromThumb(
//                                                    profileImg,
//                                                    userData.profileImage!!
//                                                )
//                                                openPopUp(userData)
                                            })
                                        binding.rvAddUser.adapter = messagesAdapter
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                        }
                    }
                }
            }
    }
}