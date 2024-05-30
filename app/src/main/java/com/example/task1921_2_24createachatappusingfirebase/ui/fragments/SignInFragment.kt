package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentSignInBinding
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants
import com.example.task1921_2_24createachatappusingfirebase.utils.Constants.USER_DB
import com.example.task1921_2_24createachatappusingfirebase.utils.checkEmail
import com.example.task1921_2_24createachatappusingfirebase.utils.checkPassword
import com.example.task1921_2_24createachatappusingfirebase.utils.removeWhiteSpaces
import com.example.task1921_2_24createachatappusingfirebase.utils.updateStatus
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var database: DatabaseReference
    private lateinit var listener: ListenerRegistration

    /* google signIn */
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private lateinit var someActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    private var emailList: ArrayList<String> = arrayListOf()
    private var passwordList: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        googleSignIn()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.apply {
            etEmailId.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    etEmailId.hint = ""
                } else {
                    etEmailId.hint = ""
                }
            }
            etPassword.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    tilPassword.hint = ""
                } else {
                    tilPassword.hint = "Enter Your Password"
                }
            }

            tvForgotPass.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_forgotPasswordFragment)
            }

            btnLogIn.setOnClickListener {
                val email = etEmailId.text.toString().trim()
                val password = etPassword.text.toString().trim()

                when {
                    !checkEmail(email) -> {
                        etEmailId.error = "Please enter valid email id"
                    }

                    !checkPassword(password) -> {
                        etPassword.error = "Please enter valid password"
                    }

                    else -> {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    findNavController().navigate(R.id.action_signInFragment_to_messagesFragment)
                                    Toast.makeText(
                                        requireContext(),
                                        "Welcome User",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Sign Up faild ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            }

            btnGoogle.setOnClickListener {
                signIn()
            }

            tvSign.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_createAccFragment)
            }
        }
    }

    private fun signIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnCompleteListener {
                try {
                    val intent =
                        IntentSenderRequest.Builder(it.result.pendingIntent.intentSender).build()
                    someActivityResultLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e("Error", "onCreate: ${e.message}")
                }
            }
    }

    private fun googleSignIn() {
        oneTapClient = Identity.getSignInClient(requireContext())
        signInRequest = BeginSignInRequest.builder()
//            .setPasswordRequestOptions(
//                BeginSignInRequest.PasswordRequestOptions.builder()
//                    .setSupported(true)
//                    .build()
//            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        someActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    oneTapClient.apiKey
                    val email = credential.id
                    val password = credential.familyName?.removeWhiteSpaces() + credential.givenName?.removeWhiteSpaces() + "@123"
                    val name = credential.displayName
                    val image = credential.profilePictureUri

                    listener = db.collection(USER_DB).addSnapshotListener { value, error ->
                        for (doc in value!!.documents) {
                            val data = doc.data
                            val userEmail = data?.get("Email") as String
                            val userPassword = data["Password"] as String

                            emailList.add(userEmail)
                            passwordList.add(userPassword)

                        }

                        for (data in emailList) {
                            for (pass in passwordList) {
                                if (email == data && password == pass) {
                                    auth.signInWithEmailAndPassword(data, pass)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                try {
                                                    findNavController().navigate(R.id.action_signInFragment_to_messagesFragment)
                                                } catch (e: Exception) {
                                                    Log.e("TAG", "${e.message}")
                                                }
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Welcome User",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    break
                                }
                            }
                        }
                    }

                    /* creating new user in firebase auth */
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = task.result?.user?.uid

                                // create user db
                                writeNewUser(
                                    uid,
                                    image.toString(),
                                    name,
                                    "offline"
                                )

                                // adding user data in firebase database
                                val userData = hashMapOf<String, Any>(
                                    Constants.USERNAME to name!!,
                                    Constants.EMAIL to email,
                                    Constants.PASSWORD to password,
                                    Constants.USERID to uid!!
                                )

                                db.collection(USER_DB).document(uid)
                                    .set(userData)
                                    .addOnCompleteListener { taskAddData ->
                                        if (taskAddData.isSuccessful) {
                                            // navigete to message fragment
                                            findNavController().navigate(R.id.action_signInFragment_to_messagesFragment)

                                            updateStatus("online")

                                            // clearing userData list
                                            userData.clear()
                                            Toast.makeText(
                                                requireContext(),
                                                "User data added successfully...",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Error adding data...",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                Toast.makeText(
                                    requireContext(),
                                    "Welcome $name",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                } catch (e: ApiException) {
                    Log.e("TAG", "signInResult:failed code=" + e.statusCode)
                }
            }
        }
    }

    private fun writeNewUser(
        currentUId: String? = null,
        profileImage: String? = null,
        userName: String? = null,
        status: String? = null
    ) {
        // adding user data in firebase database
        val userData = hashMapOf<String, Any>(
            "currentUId" to currentUId!!,
            "profileImage" to profileImage!!,
            "userName" to userName!!,
            "status" to status!!
        )

        database.child(Constants.REALTIME_DB).child(FirebaseAuth.getInstance().uid!!)
            .setValue(userData)
    }
}