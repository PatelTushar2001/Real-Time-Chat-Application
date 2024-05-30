package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawSelectedContactListBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ContactData

class SelectedContactListAdapter(
    private val contactData: ArrayList<ContactData>,
) :
    RecyclerView.Adapter<SelectedContactListAdapter.SelectedContactListHolder>() {
    private var data: MutableList<ContactData> = mutableListOf()

    inner class SelectedContactListHolder(val binding: RawSelectedContactListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedContactListHolder {
        val view = RawSelectedContactListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelectedContactListHolder(view)
    }

    override fun getItemCount(): Int {
        val data3: MutableList<ContactData> = mutableListOf()
        contactData.filter {
            data3.add(it)
        }
        return data3.toSet().size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: SelectedContactListHolder, position: Int) {

        contactData.filter {
            data.add(it)
        }

        val finalData = data.distinct()[position]
        with(holder) {
            with(binding) {
                tvSelectedContact.text = finalData.name

                ivCancel.setOnClickListener {
                    data.removeAt(position)
                    contactData.removeAll(listOf(finalData).toSet())

                    notifyDataSetChanged()
                }
            }
        }
    }
}