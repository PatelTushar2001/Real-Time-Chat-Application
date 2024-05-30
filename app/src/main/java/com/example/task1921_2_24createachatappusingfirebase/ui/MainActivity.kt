package com.example.task1921_2_24createachatappusingfirebase.ui

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.ActivityMainBinding
import com.example.task1921_2_24createachatappusingfirebase.utils.updateStatus
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_MainActivity) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.onboarding_graph)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            navGraph.setStartDestination(R.id.messagesFragment)
            Log.d("CHECKLOGIN", "onViewCreated: user logged in...")
        } else {
            navGraph.setStartDestination(R.id.letsStartFragment)
            Log.d("CHECKLOGIN", "onViewCreated: user not logged in...")
        }

        navController.graph = navGraph

        /** bottom navigation view item click event */
        val employerFragementContainerView =
            supportFragmentManager.findFragmentById(R.id.fcv_MainActivity) as NavHostFragment
        val userNavHost = employerFragementContainerView.navController
        binding.bnvMessagesView.setupWithNavController(userNavHost)

        userNavHost.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.messagesFragment -> binding.bnvMessagesView.visibility = View.VISIBLE
                R.id.addUserFragment -> binding.bnvMessagesView.visibility = View.VISIBLE
                R.id.editFragment -> binding.bnvMessagesView.visibility = View.VISIBLE
                R.id.groupFragment -> binding.bnvMessagesView.visibility = View.VISIBLE
                R.id.chatFragment -> binding.bnvMessagesView.visibility = View.GONE
                else -> {
                    binding.bnvMessagesView.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
}