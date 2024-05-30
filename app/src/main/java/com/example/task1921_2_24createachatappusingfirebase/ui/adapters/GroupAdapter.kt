package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawCreateGroupCellBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ContactData
import com.example.task1921_2_24createachatappusingfirebase.model.GroupData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData

class GroupAdapter(
    private val groupData: ArrayList<UserData>
) :
    RecyclerView.Adapter<GroupAdapter.ContactViewHolder>() {
    private var data: MutableList<UserData> = mutableListOf()

    inner class ContactViewHolder(val binding: RawCreateGroupCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = RawCreateGroupCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
        val data3: MutableList<UserData> = mutableListOf()

        groupData.filter {
            data3.add(it)
        }
        return data3.toSet().size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        groupData.filter {
            data.add(it)
        }

        val listData = data.distinct()[position]
        with(holder) {
            with(binding) {
                tvUserName.text = listData.userName
                Glide.with(itemView.context).load(listData.profileImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivProfileImg)

                itemView.rootView.setOnClickListener {
                    data.removeAt(position)
                    groupData.removeAll(listOf(listData).toSet())

                    notifyDataSetChanged()
                }
            }
        }
    }
}