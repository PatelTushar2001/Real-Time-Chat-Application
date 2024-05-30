package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.Manifest.permission.CALL_PHONE
import android.Manifest.permission.CAMERA
import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_CONTACTS
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentLetsStartBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class LetsStartFragment : Fragment() {
    private var _binding: FragmentLetsStartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLetsStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnStart.setOnClickListener {

                // all the common permissions
                Dexter.withContext(requireContext())
                    .withPermissions(
                        READ_CONTACTS,
                        WRITE_CONTACTS,
                        CALL_PHONE,
                        RECORD_AUDIO,
                        CAMERA,
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                            if (p0.isAnyPermissionPermanentlyDenied) {
                                Toast.makeText(
                                    requireContext(),
                                    "Now u have to allow permissions manually..",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            if (p0.areAllPermissionsGranted()) {
                                findNavController().navigate(R.id.action_letsStartFragment_to_signInFragment)
                                Toast.makeText(
                                    requireContext(),
                                    "All the permissions are granted..",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                findNavController().navigate(R.id.action_letsStartFragment_to_signInFragment)
                                Toast.makeText(
                                    requireContext(),
                                    "Now u have to allow permissions manually..",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken
                        ) {
                            p1.continuePermissionRequest()
                        }
                    })
                    .withErrorListener { error ->
                        Toast.makeText(
                            requireContext(),
                            "Error occurred! ${error.name}} ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onSameThread().check()


                // notification permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                POST_NOTIFICATIONS,
                            ),
                            101
                        )
                    }
                }

                //Read external storage permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            READ_EXTERNAL_STORAGE,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                READ_EXTERNAL_STORAGE,
                            ),
                            102
                        )
                    }
                }

                // write external storage permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            WRITE_EXTERNAL_STORAGE,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                WRITE_EXTERNAL_STORAGE
                            ),
                            103
                        )
                    }
                }
            }
        }

        // back press handling
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finishAndRemoveTask()
        }
    }

//    @RequiresApi(Build.VERSION_CODES.R)
//    private fun showSettingsDialog() {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Need Permissions")
//        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
//        builder.setPositiveButton("Go To Settings") { dialog, which ->
////            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
//            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
//            startActivity(intent)
//            dialog.cancel()
//        }
//        builder.setNegativeButton("Cancel") { dialog, which ->
//            dialog.cancel()
//        }
//        builder.show()
//    }
}