package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawContactListBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ContactData

class ContactAdapter(
    private val contactData: List<ContactData>,
    private val onClick: (ContactData) -> Any
) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
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
                tvContactName.text = listData.name
                tvContactNumber.text = listData.number

                itemView.rootView.setOnClickListener {
                    onClick(listData)
                }
            }
        }
    }
}