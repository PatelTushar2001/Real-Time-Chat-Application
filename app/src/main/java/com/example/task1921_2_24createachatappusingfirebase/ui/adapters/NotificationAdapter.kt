package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawContactListBinding
import com.example.task1921_2_24createachatappusingfirebase.model.NotificationData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.ui.fragments.NotificationFragmentDirections
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationAdapter(
    private val contactData: List<NotificationData>,
    private val onClick: (NotificationData) -> Any
) :
    RecyclerView.Adapter<NotificationAdapter.ContactViewHolder>() {
    inner class ContactViewHolder(val binding: RawContactListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = RawContactListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int = contactData.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val listData = contactData[position]
        with(holder) {
            with(binding) {
                tvContactName.text = listData.message
//                tvContactNumber.text = listData.number

                itemView.rootView.setOnClickListener {
                    onClick(listData)
                }
            }
        }
    }
}