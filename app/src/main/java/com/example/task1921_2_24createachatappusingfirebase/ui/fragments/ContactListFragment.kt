package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentContactListBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ContactData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.NotificationData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.PushNotification
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.Token
import com.example.task1921_2_24createachatappusingfirebase.notifications.network.RetrofitInstance
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.ContactAdapter
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.SelectedContactListAdapter
import com.example.task1921_2_24createachatappusingfirebase.utils.getFileExtention
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ContactListFragment : Fragment() {
    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactAdapter: ContactAdapter
    private var contactListData: ArrayList<ContactData> = arrayListOf()
    private var contactImageList: ArrayList<String> = arrayListOf()

    private lateinit var selectedContactAdapter: SelectedContactListAdapter
    private var selectedContactListData: ArrayList<ContactData> = arrayListOf()
    private var finalSelectedContactListData: ArrayList<ContactData> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUi()
        getContacts()

    }

    @SuppressLint("NotifyDataSetChanged", "Recycle", "Range")
    private fun getContacts() {
        contactListData.clear()

        val cursor = activity?.contentResolver
            ?.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )

        while (cursor!!.moveToNext()) {
            val image =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
//            Log.d("ContactImage", "getContacts: $image")
//
//            contactImageList.add(image)
//            val inputStream = Contacts.openContactPhotoInputStream(activity?.contentResolver, ContentUris.withAppendedId(Contacts.CONTENT_URI,))

//            if (image.isNotEmpty()) {
//                uploadImageToFirebase(
//                    image.toUri()
//                )
//            } else {
//                uploadImageToFirebase(("content://com.android.contacts/display_photo/3").toUri())
//            }


            contactListData.add(
                ContactData(
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                )
            )
        }

//        Log.d("TAGCONTACTIMAGELIST", "getContacts: $contactImageList")

//        try {
//            uploadImageToFirebase(contactImageList.first().toUri())
//        } catch (e: Exception){
//
//        }
//        contactImageList.forEach {
//            if (it.isNotEmpty()) {
//                uploadImageToFirebase(("content://com.android.contacts/display_photo/3").toUri())
//            }
//        }

        Log.d("TAGCONTACTIMAGELIST", "getContacts: $contactListData")
        contactAdapter.notifyDataSetChanged()
        cursor.close()

    }

    private fun uploadImageToFirebase(uri: Uri) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key
        val fileRef =
            FirebaseStorage.getInstance().reference.child("contacts/")
                .child(
                    "${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(
                        requireContext(),
                        uri
                    )
                )

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener {
                    val chatHashMap = hashMapOf<String, Any>(
                        "image" to it.toString()
                    )

                    FirebaseFirestore.getInstance().collection("ContactImage").document(msgKey!!)
                        .set(chatHashMap)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUi() {
        selectedContactListData.clear()
        finalSelectedContactListData.clear()

        contactAdapter = ContactAdapter(contactListData) { chatData ->

            selectedContactListData.add(chatData)
            selectedContactListData.filter { finalSelectedContactListData.add(it) }

            selectedContactAdapter = SelectedContactListAdapter(selectedContactListData)
            binding.rvSelectedContacts.adapter = selectedContactAdapter
            selectedContactAdapter.notifyDataSetChanged()
        }

        binding.apply {
            rvContactList.adapter = contactAdapter

            ivBack.setOnClickListener {

                findNavController().popBackStack()
            }

            fabAddContact.setOnClickListener {
                /* navigating data to chat fragment */
                val sharedCustomePref = requireActivity().getSharedPreferences(
                    "receiver_data",
                    FirebaseMessagingService.MODE_PRIVATE
                )
                val receiverId = sharedCustomePref?.getString("receiverId", "")
                sendMsgToUser(FirebaseAuth.getInstance().uid!!, receiverId!!)

                findNavController().popBackStack()
            }
        }
    }

    /* send text message to receiver  */
    private fun sendMsgToUser(uid: String?, selectedUid: String) {
        val sharedCustomePref = this.activity?.getSharedPreferences(
            "receiver_data",
            FirebaseMessagingService.MODE_PRIVATE
        )
        val receiverId = sharedCustomePref?.getString("receiverId", "")
        var userName = ""
        FirebaseDatabase.getInstance().reference.child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val chats = data.getValue(UserData::class.java)
                            if (chats?.currentUId == receiverId) {
                                userName = chats?.userName.toString()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
        val currentDate = sdf.format(java.util.Date())

        var contactSizeCalc = ""

        finalSelectedContactListData.distinct().forEach {
            val contactHashmap = hashMapOf(
                "name" to it.name.toString(),
                "number" to it.number.toString(),
            )
            FirebaseFirestore.getInstance().collection("Chats").document(msgKey!!)
                .collection("Contacts")
                .document().set(contactHashmap)
        }

        contactSizeCalc = when (finalSelectedContactListData.size) {
            1 -> {
                "${finalSelectedContactListData.first().name}"
            }

            2 -> {
                "${finalSelectedContactListData.first().name} and 1 other contact"
            }

            else -> {
                "${finalSelectedContactListData.first().name} and ${finalSelectedContactListData.distinct().size - 1} other contacts"
            }
        }

        val msgHashMap = hashMapOf<String, Any>(
            "sender" to uid!!,
            "message" to "sent contacts.",
            "receiver" to selectedUid,
            "timeStamp" to currentDate,
            "messageId" to msgKey!!,
            "contact" to contactSizeCalc,
            "type" to "contact"

        )
        val chatHashMap = hashMapOf<String, Any>(
            "receiver" to selectedUid,
            "isSeen" to false,
            "timeStamp" to currentDate,
        )

        FirebaseFirestore.getInstance().collection("Chats").document(msgKey).set(chatHashMap)

        ref.child("Chats")
            .child(msgKey)
            .setValue(msgHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    /* for send notification to other user when sending chat */
                    FirebaseFirestore.getInstance().collection("Tokens").document(selectedUid)
                        .addSnapshotListener { value, _ ->
                            if (value != null && value.exists()) {
                                val tokenObject = value.toObject(Token::class.java)

                                val token = tokenObject?.token

                                if (selectedUid.isNotEmpty()) {
                                    PushNotification(
                                        NotificationData(userName, "Contacts"),
                                        token!!
                                    ).also {
                                        sendNotification(it)
                                    }
                                }
                            }
                        }
                }
            }
    }

    /* post notification on message sent */
    @OptIn(DelicateCoroutinesApi::class)
    private fun sendNotification(it: PushNotification) = GlobalScope.launch {
        try {
            val response = RetrofitInstance.api.postNotification(it)
            if (response.isSuccessful) {
                Log.d("Success", "response successful...")
            }
        } catch (e: Exception) {
            Log.e("Error", "sendNotification: ${e.message}")
        }
    }
}