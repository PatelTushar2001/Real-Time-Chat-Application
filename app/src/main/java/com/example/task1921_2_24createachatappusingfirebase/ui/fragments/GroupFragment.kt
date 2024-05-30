package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentGroupBinding
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.MessagesAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupFragment : Fragment() {
    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupAdapter: MessagesAdapter
    private var groupListData: ArrayList<UserData> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnChat.setOnClickListener {
                findNavController().navigate(R.id.action_groupFragment_to_messagesFragment)
            }
        }

        FirebaseDatabase.getInstance().reference.child("Groups")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val v = snapshot.children
                    v.forEach {

                        Log.e("GROPDATATAG", "onDataChange: ${it.key}", )

                        groupListData.add((UserData("","", it.key)))

                    }

                    groupAdapter = MessagesAdapter(groupListData, {

                    }, {
                            userData, imageView ->
                    })
                    binding.rvGroups.adapter = groupAdapter

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}