package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentViewContactsBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatData
import com.example.task1921_2_24createachatappusingfirebase.model.ContactData
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.ViewContactAdapter
import com.example.task1921_2_24createachatappusingfirebase.utils.convertBitmapToUri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessagingService

class ViewContactsFragment : Fragment() {
    private var _binding: FragmentViewContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewContactAdapter: ViewContactAdapter
    private var viewContactData: ArrayList<ContactData> = arrayListOf()

    private lateinit var listener: ListenerRegistration

    private val args: ViewContactsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getContactData()
    }

    override fun onPause() {
        super.onPause()
        viewContactData.clear()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getContactData() {
        viewContactData.clear()

        FirebaseDatabase.getInstance().reference.child("Chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (data in snapshot.children){
                            val contact = data.getValue(ChatData::class.java)
                            if (contact?.messageId == args.chatData.messageId){
                                listener = FirebaseFirestore.getInstance().collection("Chats")
                                    .document(contact?.messageId!!).collection("Contacts")
                                    .addSnapshotListener { snap, exception ->
                                        if (snap != null) {
                                            for (doc in snap.documents) {
                                                val data2 = doc.data

                                                val name = data2?.get("name") as String
                                                val number = data2["number"] as String

                                                viewContactData.add(ContactData(name, number))
                                            }

                                            Log.d("ContactDATALIST", "onDataChange: $viewContactData")

                                            viewContactAdapter = ViewContactAdapter(viewContactData){
                                                addContactToPhone(it)
                                            }
                                            binding.apply {
                                                ivBack.setOnClickListener {
                                                    findNavController().popBackStack()
                                                }
                                                rvViewContacts.adapter = viewContactAdapter
                                            }
                                            viewContactAdapter.notifyDataSetChanged()
                                        }
                                    }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    /* launch gallary view */
    private val addContactLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(requireContext(), "Added Contact", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun addContactToPhone(userData: ContactData){
        val intent = Intent(ContactsContract.Intents.Insert.ACTION)
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE)
        intent.putExtra(ContactsContract.Intents.Insert.NAME, userData.name)
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, userData.number)
        addContactLauncher.launch(intent)
    }
}