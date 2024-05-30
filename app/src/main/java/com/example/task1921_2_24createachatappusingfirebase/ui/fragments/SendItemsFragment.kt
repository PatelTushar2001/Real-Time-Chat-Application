package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentSendItemsBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatData
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.NotificationData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.PushNotification
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.Token
import com.example.task1921_2_24createachatappusingfirebase.notifications.network.RetrofitInstance
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

class SendItemsFragment : Fragment() {
    private var _binding: FragmentSendItemsBinding? = null
    private val binding get() = _binding!!

    private val args: SendItemsFragmentArgs by navArgs()

    private var receiverId: String? = null
    private var userName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedCustomePref = this.activity?.getSharedPreferences(
            "receiver_data",
            FirebaseMessagingService.MODE_PRIVATE
        )

        receiverId = sharedCustomePref?.getString("receiverId", "")
        userName = ""

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

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }

            when (args.chatData.type) {
                "image" -> {
                    ivNormalFullImage.visibility = View.GONE
                    ivFullImage.visibility = View.VISIBLE

                    Glide.with(requireContext()).load(args.chatData.url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivFullImage)
                }

                "audio" -> {
                    ivFullImage.visibility = View.GONE
                    ivNormalFullImage.visibility = View.VISIBLE

                    Glide.with(requireContext()).load(R.drawable.ic_mp3)
                        .override(100,100)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivNormalFullImage)
                }

                "video" -> {
                    ivFullImage.visibility = View.GONE
                    ivNormalFullImage.visibility = View.VISIBLE

                    Glide.with(requireContext()).load(R.drawable.ic_mp4)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivNormalFullImage)
                }

                "pdf" -> {
                    ivFullImage.visibility = View.GONE
                    ivNormalFullImage.visibility = View.VISIBLE

                    Glide.with(requireContext()).load(R.drawable.picture_as_pdf)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivNormalFullImage)
                }

                "doc" -> {
                    ivFullImage.visibility = View.GONE
                    ivNormalFullImage.visibility = View.VISIBLE

                    Glide.with(requireContext()).load(R.drawable.ic_doc)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivNormalFullImage)
                }

                else -> {
                    ivFullImage.visibility = View.GONE
                    ivNormalFullImage.visibility = View.GONE
                }
            }

            fabAddContact.setOnClickListener {
                pbSendItem.visibility = View.VISIBLE
                when (args.chatData.type) {
                    "image" -> {
                        uploadImageMessageToFirebase(args.chatData.url!!.toUri())
                    }

                    "audio" -> {
                        uploadAudioUriToFirebase(
                            args.chatData.url!!.toUri(),
                            args.chatData.fileName!!,
                            args.chatData.fileExtention!!
                        )
                    }

                    "video" -> {
                        uploadVideoUriToFirebase(
                            args.chatData.url!!.toUri(),
                            args.chatData.fileName!!,
                            args.chatData.fileExtention!!
                        )
                    }

                    "pdf" -> {
                        uploadPdfUriToFirebase(
                            args.chatData.url!!.toUri(),
                            args.chatData.fileName!!
                        )
                    }

                    "doc" -> {
                        uploadDocumentsUriToFirebase(
                            args.chatData.url!!.toUri(),
                            "sent you a doc file.",
                            args.chatData.fileName!!,
                            args.chatData.fileExtention!!
                        )
                    }
                }
            }
        }
    }

    /* upload image uri to firebase */
    private fun uploadImageMessageToFirebase(uri: Uri) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val fileRef =
            FirebaseStorage.getInstance().reference.child("images/")
                .child(
                    "${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(
                        requireContext(),
                        uri
                    )
                )

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                        val currentDate = sdf.format(java.util.Date())

                        val msgHashMap = hashMapOf<String, Any>(
                            "sender" to FirebaseAuth.getInstance().uid!!,
                            "message" to "sent you an image.",
                            "receiver" to receiverId!!,
                            "timeStamp" to currentDate,
                            "messageId" to msgKey!!,
                            "url" to it.toString(),
                            "type" to "image"
                        )
                        val chatHashMap = hashMapOf<String, Any>(
                            "receiver" to receiverId!!,
                            "isSeen" to false,
                            "timeStamp" to currentDate,
                            "url" to it.toString(),
                            "message" to "sent you an image.",
                        )

                        FirebaseFirestore.getInstance().collection("Chats").document(msgKey)
                            .set(chatHashMap)

                        ref.child("Chats")
                            .child(msgKey)
                            .setValue(msgHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    binding.pbSendItem.visibility = View.GONE
                                    findNavController().popBackStack()

                                    /* for send notification to other user when sending chat */
                                    FirebaseFirestore.getInstance().collection("Tokens")
                                        .document(receiverId!!)
                                        .addSnapshotListener { value, _ ->
                                            if (value != null && value.exists()) {
                                                val tokenObject = value.toObject(Token::class.java)

                                                val token = tokenObject?.token

                                                if (receiverId!!.isNotEmpty()) {
                                                    PushNotification(
                                                        NotificationData(
                                                            userName!!,
                                                            "image"
                                                        ),
                                                        token!!
                                                    ).also { noti ->
                                                        sendNotification(noti)
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                    }
                    .addOnFailureListener {
                        binding.pbSendItem.visibility = View.GONE
                        Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                binding.pbSendItem.visibility = View.GONE
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
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

    /* upload audio file to firebase */
    private fun uploadAudioUriToFirebase(audio: Uri, displayName: String, fileExtention: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val fileRef =
            FirebaseStorage.getInstance().reference.child("audio/")
                .child(
                    "${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(
                        requireContext(),
                        audio
                    )
                )

        fileRef.putFile(audio)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                        val currentDate = sdf.format(java.util.Date())

                        val msgHashMap = hashMapOf<String, Any>(
                            "sender" to FirebaseAuth.getInstance().uid!!,
                            "message" to "sent you a audio.",
                            "receiver" to receiverId!!,
                            "timeStamp" to currentDate,
                            "messageId" to msgKey!!,
                            "url" to it.toString(),
                            "fileName" to displayName,
                            "fileExtention" to fileExtention,
                            "type" to "audio"
                        )
                        val chatHashMap = hashMapOf<String, Any>(
                            "receiver" to receiverId!!,
                            "isSeen" to false,
                            "timeStamp" to currentDate,
                            "url" to it.toString(),
                            "message" to "sent you a audio.",
                            "fileName" to displayName,
                            "fileExtention" to fileExtention
                        )

                        FirebaseFirestore.getInstance().collection("Chats").document(msgKey)
                            .set(chatHashMap)

                        ref.child("Chats")
                            .child(msgKey)
                            .setValue(msgHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    binding.pbSendItem.visibility = View.GONE
                                    findNavController().popBackStack()

                                    /* for send notification to other user when sending chat */
                                    FirebaseFirestore.getInstance().collection("Tokens")
                                        .document(receiverId!!)
                                        .addSnapshotListener { value, _ ->
                                            if (value != null && value.exists()) {
                                                val tokenObject = value.toObject(Token::class.java)

                                                val token = tokenObject?.token

                                                if (receiverId!!.isNotEmpty()) {
                                                    PushNotification(
                                                        NotificationData(
                                                            userName!!,
                                                            "Audio"
                                                        ),
                                                        token!!
                                                    ).also { noti ->
                                                        sendNotification(noti)
                                                    }
                                                }
                                            }
                                        }

                                }
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
    }

    /* upload video file to firebase */
    private fun uploadVideoUriToFirebase(video: Uri, displayName: String, fileExtention: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val fileRef =
            FirebaseStorage.getInstance().reference.child("video/")
                .child(
                    "${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(
                        requireContext(),
                        video
                    )
                )

        fileRef.putFile(video)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                        val currentDate = sdf.format(java.util.Date())

                        val msgHashMap = hashMapOf<String, Any>(
                            "sender" to FirebaseAuth.getInstance().uid!!,
                            "message" to "sent you a video.",
                            "receiver" to receiverId!!,
                            "timeStamp" to currentDate,
                            "messageId" to msgKey!!,
                            "url" to it.toString(),
                            "fileName" to displayName,
                            "fileExtention" to fileExtention,
                            "type" to "video"
                        )
                        val chatHashMap = hashMapOf<String, Any>(
                            "receiver" to receiverId!!,
                            "isSeen" to false,
                            "timeStamp" to currentDate,
                            "url" to it.toString(),
                            "message" to "sent you a video.",
                            "fileName" to displayName,
                            "fileExtention" to fileExtention
                        )

                        FirebaseFirestore.getInstance().collection("Chats").document(msgKey)
                            .set(chatHashMap)

                        ref.child("Chats")
                            .child(msgKey)
                            .setValue(msgHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    binding.pbSendItem.visibility = View.GONE
                                    findNavController().popBackStack()

                                    /* for send notification to other user when sending chat */
                                    FirebaseFirestore.getInstance().collection("Tokens")
                                        .document(receiverId!!)
                                        .addSnapshotListener { value, _ ->
                                            if (value != null && value.exists()) {
                                                val tokenObject = value.toObject(Token::class.java)

                                                val token = tokenObject?.token

                                                if (receiverId!!.isNotEmpty()) {
                                                    PushNotification(
                                                        NotificationData(
                                                            userName!!,
                                                            "Video"
                                                        ),
                                                        token!!
                                                    ).also { noti ->
                                                        sendNotification(noti)
                                                    }
                                                }
                                            }
                                        }

                                }
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
    }

    /* upload PDF file to Firebase realtime database */
    private fun uploadPdfUriToFirebase(pdf: Uri, displayName: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val fileRef =
            FirebaseStorage.getInstance().reference.child("pdf/")
                .child(
                    "${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(
                        requireContext(),
                        pdf
                    )
                )

        fileRef.putFile(pdf)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                        val currentDate = sdf.format(java.util.Date())

                        val msgHashMap = hashMapOf<String, Any>(
                            "sender" to FirebaseAuth.getInstance().uid!!,
                            "message" to "sent you a pdf.",
                            "receiver" to receiverId!!,
                            "timeStamp" to currentDate,
                            "messageId" to msgKey!!,
                            "url" to it.toString(),
                            "fileName" to displayName,
                            "type" to "pdf"
                        )
                        val chatHashMap = hashMapOf<String, Any>(
                            "receiver" to receiverId!!,
                            "isSeen" to false,
                            "timeStamp" to currentDate,
                            "url" to it.toString(),
                            "message" to "sent you a pdf.",
                            "fileName" to displayName
                        )

                        FirebaseFirestore.getInstance().collection("Chats").document(msgKey)
                            .set(chatHashMap)

                        ref.child("Chats")
                            .child(msgKey)
                            .setValue(msgHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    binding.pbSendItem.visibility = View.GONE
                                    findNavController().popBackStack()

                                    /* for send notification to other user when sending chat */
                                    FirebaseFirestore.getInstance().collection("Tokens")
                                        .document(receiverId!!)
                                        .addSnapshotListener { value, _ ->
                                            if (value != null && value.exists()) {
                                                val tokenObject = value.toObject(Token::class.java)

                                                val token = tokenObject?.token

                                                if (receiverId!!.isNotEmpty()) {
                                                    PushNotification(
                                                        NotificationData(
                                                            userName!!,
                                                            "PDF"
                                                        ),
                                                        token!!
                                                    ).also { noti ->
                                                        sendNotification(noti)
                                                    }
                                                }
                                            }
                                        }

                                }
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
    }

    /* upload different document file to firebase */
    private fun uploadDocumentsUriToFirebase(
        uri: Uri,
        message: String,
        displayName: String,
        fileExtention: String
    ) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val fileRef =
            FirebaseStorage.getInstance().reference.child("$fileExtention/")
                .child(
                    "${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(
                        requireContext(),
                        uri
                    )
                )

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                        val currentDate = sdf.format(java.util.Date())

                        val msgHashMap = hashMapOf<String, Any>(
                            "sender" to FirebaseAuth.getInstance().uid!!,
                            "message" to message,
                            "receiver" to receiverId!!,
                            "timeStamp" to currentDate,
                            "messageId" to msgKey!!,
                            "url" to it.toString(),
                            "fileName" to displayName,
                            "fileExtention" to fileExtention,
                            "type" to "doc"
                        )
                        val chatHashMap = hashMapOf<String, Any>(
                            "receiver" to receiverId!!,
                            "isSeen" to false,
                            "timeStamp" to currentDate,
                            "url" to it.toString(),
                            "message" to message,
                            "fileName" to displayName,
                            "fileExtention" to fileExtention
                        )

                        FirebaseFirestore.getInstance().collection("Chats").document(msgKey)
                            .set(chatHashMap)

                        ref.child("Chats")
                            .child(msgKey)
                            .setValue(msgHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    binding.pbSendItem.visibility = View.GONE
                                    findNavController().popBackStack()

                                    /* for send notification to other user when sending chat */
                                    FirebaseFirestore.getInstance().collection("Tokens")
                                        .document(receiverId!!)
                                        .addSnapshotListener { value, _ ->
                                            if (value != null && value.exists()) {
                                                val tokenObject = value.toObject(Token::class.java)

                                                val token = tokenObject?.token

                                                if (receiverId!!.isNotEmpty()) {
                                                    PushNotification(
                                                        NotificationData(
                                                            userName!!,
                                                            message
                                                        ),
                                                        token!!
                                                    ).also { noti ->
                                                        sendNotification(noti)
                                                    }
                                                }
                                            }
                                        }

                                }
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
    }
}