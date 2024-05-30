package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnSubmit.setOnClickListener(this@ForgotPasswordFragment)
            ivBack.setOnClickListener(this@ForgotPasswordFragment)
        }
    }

    override fun onClick(v: View?) {
        with(binding){
            val email = etEmailId.text.toString().trim()
            when(v){
                ivBack -> {
                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInFragment)
                }
                btnSubmit -> {
                    if (email.isNotEmpty()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Email sent successfully to reset your password!!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInFragment)

                                } else {
                                    Toast.makeText(
                                        context,
                                        "${task.exception!!.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            }
        }
    }
}