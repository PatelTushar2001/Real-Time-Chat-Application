package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentProfileBinding
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileFragmentArgs by navArgs()

    private lateinit var seenListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            tvPersonName.text = args.userData.userName
        }

        defaultProfileImage(args.userData.currentUId!!)

        /** back press handling*/
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finishAndRemoveTask()
        }
    }

    private fun defaultProfileImage(selectedUid: String) {
        // default image value
        val userRef = FirebaseDatabase.getInstance().reference
        seenListener = userRef.child(Constants.REALTIME_DB).child(selectedUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val user = snapshot.getValue(UserData::class.java)

                            Glide.with(requireContext()).load(user?.profileImage)
                                .apply(RequestOptions().circleCrop())
                                .placeholder(R.drawable.ic_person)
                                .into(binding.ivEditImage)
                        } catch (e: Exception) {
                            Log.e("Error", "${e.message}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}