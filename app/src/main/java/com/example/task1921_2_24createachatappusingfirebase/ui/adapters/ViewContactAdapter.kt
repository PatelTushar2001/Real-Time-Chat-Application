package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawViewContactsCellBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ContactData

class ViewContactAdapter(
    private val contactData: List<ContactData>,
    private val onClick: (ContactData) -> Any
) : RecyclerView.Adapter<ViewContactAdapter.ViewContactsViewHolder>() {
    inner class ViewContactsViewHolder(val binding: RawViewContactsCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewContactsViewHolder {
        val view =
            RawViewContactsCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewContactsViewHolder(view)
    }

    override fun getItemCount(): Int = contactData.size

    override fun onBindViewHolder(holder: ViewContactsViewHolder, position: Int) {
        val data = contactData[position]
        with(holder) {
            with(binding) {
                tvContactName.text = data.name
                tvContactNumber.text = data.number

                btnAddContact.setOnClickListener {
                    onClick(data)
                }
            }
        }
    }
}