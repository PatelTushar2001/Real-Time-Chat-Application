package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentChatBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatData
import com.example.task1921_2_24createachatappusingfirebase.model.SendChatData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.NotificationData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.PushNotification
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.Token
import com.example.task1921_2_24createachatappusingfirebase.notifications.network.RetrofitInstance
import com.example.task1921_2_24createachatappusingfirebase.ui.adapters.ChatAdapter
import com.example.task1921_2_24createachatappusingfirebase.utils.changeStateOfMessage
import com.example.task1921_2_24createachatappusingfirebase.utils.convertBitmapToUri
import com.example.task1921_2_24createachatappusingfirebase.utils.getFileExtention
import com.example.task1921_2_24createachatappusingfirebase.utils.removeWhiteSpaces
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Locale

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val args: ChatFragmentArgs by navArgs()

    private lateinit var chatsAdapter: ChatAdapter
    private val chatListData: ArrayList<ChatData> = arrayListOf()
    private val userChatData: ArrayList<ChatData> = arrayListOf()

    private var notify = false

    /* update isseen state of message */
    private lateinit var seenListener: ValueEventListener
    private lateinit var unknownUserListener: ValueEventListener

    /* media recorder */
    private lateinit var mediaRecorder: MediaRecorder

    private lateinit var tempMediaOutput: String
    var mediaState = false
    private val recAudioName = "/RecordedAudio.mp3"
    private var path: String? = null

    private val REQUEST_PERMISSION_CODE = 1001

    /* launch gallary view */
    @SuppressLint("Range")
    private val gallaryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    val uri = it.toString()
                    val fileExtention = getFileExtention(requireContext(), it)

                    val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                    val currentDate = sdf.format(java.util.Date())

                    val myFile = File(uri)
                    myFile.absolutePath
                    var displayName = ""

                    if (uri.startsWith("content://")) {
                        var cursor: Cursor? = null

                        try {
                            cursor =
                                activity?.contentResolver?.query(it, null, null, null, null)
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName =
                                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            }
                        } finally {
                            cursor?.close()
                        }
                    } else if (uri.startsWith("file://")) {
                        displayName = myFile.name
                    }

                    val action = ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                        SendChatData(fileExtention, displayName, currentDate, "image", uri)
                    )
                    findNavController().navigate(action)

                }
            } else {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            }
        }

    /* launch gallary view */
    @SuppressLint("Range")
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photo = result.data?.extras?.get("data") as Bitmap
                val uri = convertBitmapToUri(requireContext(), photo)
                val fileExtention = getFileExtention(requireContext(), uri)

                val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                val currentDate = sdf.format(java.util.Date())

                val myFile = File(uri.toString())
                myFile.absolutePath
                var displayName = ""

                if (uri.toString().startsWith("content://")) {
                    var cursor: Cursor? = null

                    try {
                        cursor =
                            activity?.contentResolver?.query(uri, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    } finally {
                        cursor?.close()
                    }
                } else if (uri.toString().startsWith("file://")) {
                    displayName = myFile.name
                }

                val action = ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                    SendChatData(fileExtention, displayName, currentDate, "image", uri.toString())
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            }
        }

    /* launch pdf picker view */
    @SuppressLint("Range")
    private val documentFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {


                    val uri = it.toString()
                    val fileExtention = getFileExtention(requireContext(), it)
                    Log.d("fileExtention", "$uri")
                    Log.d("fileExtention", "$fileExtention")

                    val myFile = File(uri)
                    myFile.absolutePath
                    var displayName = ""

                    if (uri.startsWith("content://")) {
                        var cursor: Cursor? = null

                        try {
                            cursor =
                                activity?.contentResolver?.query(it, null, null, null, null)
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName =
                                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                            }
                        } finally {
                            cursor?.close()
                        }
                    } else if (uri.startsWith("file://")) {
                        displayName = myFile.name
                    }

                    val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                    val currentDate = sdf.format(java.util.Date())

                    when (fileExtention) {
                        // audio
                        "mp3", "AAC", "AIFF", "WMA" -> {

                            val action =
                                ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                                    SendChatData(
                                        fileExtention,
                                        displayName,
                                        currentDate,
                                        "audio",
                                        uri
                                    )
                                )
                            findNavController().navigate(action)
                        }

                        // video
                        "mp4", "MOV", "AVI", "WMV", "mkv" -> {

                            val action =
                                ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                                    SendChatData(
                                        fileExtention,
                                        displayName,
                                        currentDate,
                                        "video",
                                        uri
                                    )
                                )
                            findNavController().navigate(action)
                        }

                        // image
                        "jpg", "jpeg", "jfif", "pjpeg", "pjp", "png", "webp", "avif", "apng", "tif", "tiff", "gif" -> {

                            val action =
                                ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                                    SendChatData(
                                        fileExtention,
                                        displayName,
                                        currentDate,
                                        "image",
                                        uri
                                    )
                                )
                            findNavController().navigate(action)
                        }

                        // pdf
                        "pdf" -> {
                            val action =
                                ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                                    SendChatData(
                                        fileExtention,
                                        displayName,
                                        currentDate,
                                        "pdf",
                                        uri
                                    )
                                )
                            findNavController().navigate(action)
                        }
                        // doc
                        "docx", "bin", "pptx", "txt", "apk", "zip" -> {

                            val action =
                                ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                                    SendChatData(
                                        fileExtention,
                                        displayName,
                                        currentDate,
                                        "doc",
                                        uri
                                    )
                                )
                            findNavController().navigate(action)
                        }
                    }
                    Log.d("PDFFILEDATA", fileExtention!!)
                }
            } else {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            }
        }

    /* launch pdf picker view */
    @SuppressLint("Range")
    private val callRecordingFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {

                    val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                    val currentDate = sdf.format(java.util.Date())

                    val uri = it.toString()
                    val fileExtention = getFileExtention(requireContext(), it)
                    val myFile = File(uri)
                    myFile.absolutePath
                    var displayName = ""

                    if (uri.startsWith("content://")) {
                        var cursor: Cursor? = null

                        try {
                            cursor = activity?.contentResolver?.query(it, null, null, null, null)
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName =
                                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            }
                        } finally {
                            cursor?.close()
                        }
                    } else if (uri.startsWith("file://")) {
                        displayName = myFile.name
                    }

                    val action = ChatFragmentDirections.actionChatFragmentToSendItemsFragment(
                        SendChatData(fileExtention, displayName, currentDate, "audio", uri)
                    )
                    findNavController().navigate(action)
                }
            } else {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.rvMessages.smoothScrollToPosition(chatListData.count())

        changeStateOfMessage(args.userData.currentUId!!, "seen")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UseCompatLoadingForDrawables", "Range")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isSeenMessage(args.userData.currentUId!!)

        /* display dialog for unknown user */
        val unknownUserRef = FirebaseDatabase.getInstance().reference.child("ChatList")


        unknownUserListener = unknownUserRef.child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val idValid = data.child("isValidUser").getValue(String::class.java)

//                        val updateChatListToBlock = hashMapOf<String, Any>(
//                            "isValidUser" to "block"
//                        )
//                        val updateChatListToContinue = hashMapOf<String, Any>(
//                            "isValidUser" to "continue"
//                        )

                        if (idValid == "") {
                            binding.apply {
//                                clyUnknownUserOptions.visibility = View.VISIBLE

//                                tvUnknownUserName.text = String.format(
//                                    context!!.getString(R.string.lbl_title_unknown_user),
//                                    args.userData.userName
//                                )
//
//                                /* block item click event */
//                                ivBlock.setOnClickListener {
////                                    unknownUserRef.child(FirebaseAuth.getInstance().uid!!).child(args.userData.currentUId!!)
////                                        .updateChildren(updateChatListToBlock)
//                                }

                                /* continue item click event */
//                                ivContinue.setOnClickListener {
////                                    unknownUserRef.child(FirebaseAuth.getInstance().uid!!).child(args.userData.currentUId!!)
////                                        .updateChildren(updateChatListToContinue)
//                                }

                            }

                        } else {
//                            binding.clyUnknownUserOptions.visibility = View.GONE
                        }

                        Log.d("USERDETAILS", "onDataChange: ${idValid}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        tempMediaOutput =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString()


        path = File(tempMediaOutput, recAudioName).toString()

        mediaRecorder = MediaRecorder()
        mediaRecorder.setOutputFile(path)

        Log.d("selectedUid", "onDataChange: ${args.userData.currentUId!!}")

        retrieveMessages(FirebaseAuth.getInstance().uid, args.userData.currentUId!!)

        binding.apply {
            setVideoCall(args.userData.userName.toString().removeWhiteSpaces())
            setVoiceCall(args.userData.userName.toString().removeWhiteSpaces())

            Glide.with(requireContext()).load(args.userData.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions().circleCrop())
                .placeholder(R.drawable.ic_person)
                .into(ivProfileImg)

            tvUserName.text = args.userData.userName
            tvStatus.text = args.userData.status

            tilMessage.setStartIconOnClickListener {
                /* bottom sheet dialog */
                displayBottomSheetDialog({
                    // gallary
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    gallaryLauncher.launch(intent)

                }, {
                    // document
                    val intent =
                        Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    documentFileLauncher.launch(intent)
                }, {
                    // camera
                    val intent =
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(intent)
                }, {
                    // contact
                    /* navigating data to contact fragment */
                    findNavController().navigate(R.id.action_chatFragment_to_contactListFragment)
                }, {
                    // audio
//                    val path2 =
//                        Environment.getExternalStorageDirectory()
//                            .toString() + "/" + "Music" + "/" + "Recordings" + "/" + "Call Recordings" + "/"
//                    val uri = Uri.parse(path2)

                    val intent =
                        Intent(Intent.ACTION_GET_CONTENT)
                    intent.setType("audio/*")
                    callRecordingFileLauncher.launch(intent)

//                    val path3 =
//                        Environment.getExternalStorageDirectory().toString() + "/" + "Music" + "/"
//                    val uri = Uri.parse(path3)
//                    val intent = Intent(Intent.ACTION_PICK)
//                    intent.setDataAndType(uri, "*/*")
//                    startActivity(intent)

                }, {
                    findNavController().navigate(R.id.action_chatFragment_to_mapFragment)
                })
            }

            tilMessage.setEndIconOnClickListener {
                notify = true
                val message = etEnterMsg.text.toString()
                if (message.isNotEmpty()) {
                    sendMsgToUser(
                        FirebaseAuth.getInstance().uid,
                        args.userData.currentUId!!,
                        message
                    )
                }
                etEnterMsg.text?.clear()
                etEnterMsg.refreshDrawableState()
            }

            etEnterMsg.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    tilMessage.hint = ""
                } else {
                    tilMessage.hint = "Type your message"
                }
            }

            ivBack.setOnClickListener {
                findNavController().navigate(R.id.action_chatFragment_to_messagesFragment)
            }

            ivRecordAudio.setOnClickListener {
                if (arePermissionsGranted()) {
                    handleAudioRecording()
                } else {
                    requestPermissions()
                }
            }
            clyUserActionBar.setOnClickListener {
                // open user profile page
                val action =
                    ChatFragmentDirections.actionChatFragmentToProfileFragment(args.userData)
                findNavController().navigate(action)
            }
        }

        /** back press handling*/
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseDatabase.getInstance().reference.child("Chats").removeEventListener(seenListener)
    }

    private fun arePermissionsGranted(): Boolean {
        val recordAudioPermission = ContextCompat.checkSelfPermission(requireContext(), RECORD_AUDIO)
        val writeStoragePermission = ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
        return recordAudioPermission == PackageManager.PERMISSION_GRANTED &&
                writeStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    private fun handleAudioRecording() {
        when (binding.ivRecordAudio.drawable.constantState) {
            resources.getDrawable(R.drawable.ic_microphone, null).constantState -> startRecording()
            resources.getDrawable(R.drawable.ic_recording, null).constantState -> stopRecording()
        }
    }

    private fun startRecording() {
        try {
            binding.ivRecordAudio.setImageResource(R.drawable.ic_recording)
            val fileName = "${requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath}/RecordedAudio.aac"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(fileName)
                prepare()
                start()
            }
            binding.etEnterMsg.isEnabled = false
            mediaState = true
        } catch (e: Exception) {
            Log.e("ErrorRecordingAudio", "startRecording: ${e.message}")
        }
    }

    private fun stopRecording() {
        if (mediaState) {
            mediaState = false
            binding.etEnterMsg.isEnabled = true
            binding.ivRecordAudio.setImageResource(R.drawable.ic_microphone)
            try {
                mediaRecorder.apply {
                    stop()
                    reset()
                    release()
                }
                uploadRecordedAudio()
            } catch (e: Exception) {
                Log.e("ErrorStoppingMediaRecorder", "stopRecording: ${e.message}")
            }
        }
    }

    private fun uploadRecordedAudio() {
        val fileName = "${requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath}/RecordedAudio.aac"
        val uri = Uri.fromFile(File(fileName))
        Log.d("encodedAudioString", uri.toString())
        uploadAudioUriToFirebase(uri, "", "voice")
    }

    /* send text message to receiver  */
    private fun sendMsgToUser(uid: String?, selectedUid: String, message: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
        val currentDate = sdf.format(java.util.Date())

        userChatData.add(ChatData(currentDate, selectedUid, uid))

        val msgHashMap = hashMapOf<String, Any>(
            "sender" to uid!!,
            "message" to message,
            "receiver" to selectedUid,
            "timeStamp" to currentDate,
            "messageId" to msgKey!!,
            "url" to "",
            "fileName" to "",
            "fileExtention" to "",
            "type" to "text",
            "state" to "sent"
        )
        val chatHashMap = hashMapOf<String, Any>(
            "receiver" to selectedUid,
            "isSeen" to false,
            "timeStamp" to currentDate,
            "url" to "",
            "state" to "sent",
            "message" to message,
            "sender" to uid,
        )

        FirebaseFirestore.getInstance().collection("Chats").document(msgKey).set(chatHashMap)

        ref.child("Chats")
            .child(msgKey)
            .setValue(msgHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    changeStateOfMessage(args.userData.currentUId!!, "received")

                    /* for send notification to other user when sending chat */
                    FirebaseFirestore.getInstance().collection("Tokens").document(selectedUid)
                        .addSnapshotListener { value, _ ->
                            if (value != null && value.exists()) {
                                val tokenObject = value.toObject(Token::class.java)

                                val token = tokenObject?.token

                                if (message.isNotEmpty() && selectedUid.isNotEmpty()) {
                                    PushNotification(
                                        NotificationData(args.userData.status!!, message),
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

    /* get messages list from realtime database */
    private fun retrieveMessages(senderId: String?, recieverId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("IntentReset")
            override fun onDataChange(snapshot: DataSnapshot) {
                chatListData.clear()
                for (data in snapshot.children) {
                    val chat = data.getValue(ChatData::class.java)

                    if (chat?.receiver == senderId && chat?.sender == recieverId || chat?.receiver == recieverId && chat.sender == senderId) {
                        chatListData.add(chat)
                        binding.rvMessages.smoothScrollToPosition(chatListData.count() - 1)
//                        binding.nsvChat.post {
//                            binding.nsvChat.fullScroll(View.FOCUS_DOWN)
//                        }
                        Log.d("MESSAGEINFO", "onDataChange: ${chat.messageId}")
                    }

                    chatsAdapter = ChatAdapter(requireContext(), chatListData) { chatData ->
                        if (chatData.type == "location") {
                           /* // Generate map link
                            val mapLink = "http://maps.google.com/maps?q=${chatData.lat},${chatData.long}"

                            // Create share intent
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, mapLink)
                                putExtra(Intent.EXTRA_STREAM, chatData.url)
                                type = "image/*"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            // Share location
                            startActivity(Intent.createChooser(shareIntent, "Share Location"))*/
                        */
                            val uri = "geo:${chatData.lat},${chatData.long}?q=${chatData.lat},${chatData.long}(${chatData.fileName})"
                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            mapIntent.setPackage("com.google.android.apps.maps") // Open in Google Maps app
                            startActivity(mapIntent)
                        }
                        if (chatData.type == "image") {
                            /* navigating data to chat fragment */
                            val direction =
                                ChatFragmentDirections.actionChatFragmentToViewFullImageFragment(
                                    chatData.url.toString(),
                                    false,
                                    ""
                                )
                            findNavController().navigate(direction)
                        }
                        if (chatData.message == "sent you a pdf.") {
                            /* navigating data to PDF View fragment */
                            val direction =
                                ChatFragmentDirections.actionChatFragmentToPdfViewFragment(
                                    chatData
                                )
                            findNavController().navigate(direction)
//                            val intent = Intent(Intent.ACTION_VIEW)
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            intent.setDataAndType(Uri.parse(chatData.url), "application/pdf")
//                            startActivity(intent)
                        }
                        if (chatData.message == "sent contacts.") {
                            /* navigating to Contact View fragment */
                            val direction =
                                ChatFragmentDirections.actionChatFragmentToViewContactsFragment(
                                    chatData
                                )
                            findNavController().navigate(direction)
                        }
                        if (chatData.message == "sent you a audio.") {
                            /* navigating to Contact View fragment */
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(chatData.url?.toUri(), "audio/*")
                            startActivity(intent)
                        }
                        if (chatData.message == "sent you a video.") {
                            /* navigating to Contact View fragment */
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(chatData.url?.toUri(), "video/*")
                            startActivity(intent)
                        }
                        if (chatData.message == "sent you a doc file.") {
                            /* navigating to media View */
                            Log.d("DOCXFILEURI", "onDataChange: ${chatData.url?.toUri()}")

//                            val mime = activity?.contentResolver?.getType(chatData.url?.toUri()!!)
//                            val intent = Intent(Intent.ACTION_VIEW)
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            intent.setDataAndType(Uri.parse(chatData.url), mime)
//                            startActivity(intent)
                            val direction =
                                ChatFragmentDirections.actionChatFragmentToPdfViewFragment(
                                    chatData
                                )
                            findNavController().navigate(direction)
                        }
                    }
                    binding.rvMessages.adapter = chatsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /* update isseen property of user chats */
    private fun isSeenMessage(userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val chat = data.getValue(ChatData::class.java)

                    Log.d("CHATDATA2", "onDataChange: ${chat?.messageId}")

                    if (chat?.receiver == FirebaseAuth.getInstance().uid && chat?.sender == userId) {
                        FirebaseFirestore.getInstance().collection("Chats")
                            .document(chat.messageId!!).update("isSeen", true)
                            .addOnCompleteListener { }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /* custom bottom sheet dialog */
    @SuppressLint("InflateParams")
    private fun displayBottomSheetDialog(
        onGallaryClick: () -> Any,
        onDocumentClick: () -> Any,
        onCameraClick: () -> Any,
        onContactClick: () -> Any,
        onAudioClick: () -> Any,
        onLocationClick: () -> Any,
    ) {
        val dialog = BottomSheetDialog(requireContext(), R.style.Theme_BottomSheetTheme)
        val customView =
            LayoutInflater.from(context).inflate(R.layout.custom_getfile_btm_dialog, null)

        val gallaryView = customView.findViewById<ConstraintLayout>(R.id.view_Gallary)
        val cameraView = customView.findViewById<ConstraintLayout>(R.id.view_Camera)
        val documentView = customView.findViewById<ConstraintLayout>(R.id.view_Document)
        val contactView = customView.findViewById<ConstraintLayout>(R.id.view_Contact)
        val audioView = customView.findViewById<ConstraintLayout>(R.id.view_Music)
        val locationView = customView.findViewById<ConstraintLayout>(R.id.view_Location)

        /* open gallary */
        gallaryView.setOnClickListener {
            onGallaryClick()
            dialog.dismiss()
        }

        /* open location */
        locationView.setOnClickListener {
            onLocationClick()
            dialog.dismiss()
        }

        /* get document btn click event */
        documentView.setOnClickListener {
            onDocumentClick()
            dialog.dismiss()
        }

        /* open camera on btn click */
        cameraView.setOnClickListener {
            onCameraClick()
            dialog.dismiss()
        }

        /* open contact list on btn click */
        contactView.setOnClickListener {
            onContactClick()
            dialog.dismiss()
        }

        /* open audio list on btn click */
        audioView.setOnClickListener {
            onAudioClick()
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.setContentView(customView)
        dialog.show()
    }

    private fun setVideoCall(uid: String?) {
        binding.ivVideoCall.setIsVideoCall(true)
        binding.ivVideoCall.resourceID = "zego_uikit_call"
        binding.ivVideoCall.setInvitees(Collections.singletonList(ZegoUIKitUser(uid)))
    }

    private fun setVoiceCall(uid: String?) {
        binding.ivCall.setIsVideoCall(false)
        binding.ivCall.resourceID = "zego_uikit_call"
        binding.ivCall.setInvitees(Collections.singletonList(ZegoUIKitUser(uid)))
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

                        userChatData.add(
                            ChatData(
                                currentDate,
                                args.userData.currentUId!!,
                                FirebaseAuth.getInstance().uid
                            )
                        )

                        val msgHashMap = hashMapOf<String, Any>(
                            "sender" to FirebaseAuth.getInstance().uid!!,
                            "message" to "sent you a audio.",
                            "receiver" to args.userData.currentUId!!,
                            "timeStamp" to currentDate,
                            "messageId" to msgKey!!,
                            "url" to it.toString(),
                            "fileName" to displayName,
                            "fileExtention" to fileExtention,
                            "type" to "audio"
                        )
                        val chatHashMap = hashMapOf<String, Any>(
                            "receiver" to args.userData.currentUId!!,
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
                                    /* for send notification to other user when sending chat */
                                    FirebaseFirestore.getInstance().collection("Tokens")
                                        .document(args.userData.currentUId!!)
                                        .addSnapshotListener { value, _ ->
                                            if (value != null && value.exists()) {
                                                val tokenObject = value.toObject(Token::class.java)

                                                val token = tokenObject?.token

                                                if (args.userData.currentUId!!.isNotEmpty()) {
                                                    PushNotification(
                                                        NotificationData(
                                                            args.userData.status!!,
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

}