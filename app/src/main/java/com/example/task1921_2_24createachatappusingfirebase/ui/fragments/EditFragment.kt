package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentEditBinding
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.REALTIME_DB
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.USER_DB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private lateinit var root: DatabaseReference

    /* update isseen state of message */
    private lateinit var seenListener: ValueEventListener
    private lateinit var listener: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        root = FirebaseDatabase.getInstance().reference

        listener = FirebaseFirestore.getInstance().collection(USER_DB)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val data = doc.data
                        val uName = data?.get("UserName") as String
                        val email = data["Email"] as String
                        val password = data["Password"] as String
                        val uid = data["UserID"] as String

                        if (FirebaseAuth.getInstance().uid == uid) {
                            binding.apply {
                                etUserName.setText(uName)
                                etEmail.setText(email)
                                etPassword.setText(password)
                            }
                        }
                    }
                }
            }

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().navigate(R.id.action_editFragment_to_messagesFragment)
            }

            ivAddImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                gallaryLauncher.launch(intent)
            }
            btnSignOut.setOnClickListener {
                etUserName.setText("")
                etEmail.setText("")
                etPassword.setText("")
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(), "Signing out..", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editFragment_to_signInFragment)
            }
            tilUserName.setEndIconOnClickListener {
                etUserName.isEnabled = true
            }
            tilEmail.setEndIconOnClickListener {
                etEmail.isEnabled = true
            }
//            tilPassword.pass {
//                tilPassword.isSelected = !tilPassword.isSelected
//            }

            defaultProfileImage()
        }
    }

    override fun onResume() {
        super.onResume()
        defaultProfileImage()
    }

    override fun onPause() {
        super.onPause()
        FirebaseDatabase.getInstance().reference.child(REALTIME_DB)
            .removeEventListener(seenListener)

    }

    private fun defaultProfileImage() {
        // default image value
        val userRef = FirebaseDatabase.getInstance().reference
        seenListener = userRef.child(REALTIME_DB).child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val user = snapshot.getValue(UserData::class.java)
                            Glide.with(requireContext()).load(user?.profileImage)
                                .apply(RequestOptions().circleCrop())
                                .placeholder(R.drawable.ic_person)
//                                .error(R.drawable.ic_no_img)
                                .into(binding.ivEditImage)
                        } catch (e: Exception) {
                            Log.e("Error", "${e.message}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun uploadToFirebase(uri: Uri) {
        val fileRef =
            FirebaseStorage.getInstance().reference.child("images/")
                .child("${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(uri))
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener {
                        val userData = hashMapOf<String, Any>(
                            "profileImage" to it.toString()
                        )

                        root.child(REALTIME_DB).child(FirebaseAuth.getInstance().uid!!)
                            .updateChildren(userData)
                        Toast.makeText(requireContext(), "Upload Success...", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getFileExtention(uri: Uri): String? {
        val cr: ContentResolver = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))
    }

    private val gallaryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    imageUri = it
                    Glide.with(this).load(it)
                        .apply(RequestOptions().circleCrop())
                        .placeholder(R.drawable.ic_person)
                        .into(binding.ivEditImage)

                    if (imageUri != null) {
                        uploadToFirebase(imageUri!!)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            }
        }
}
