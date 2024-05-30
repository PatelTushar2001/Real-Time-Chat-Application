package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentMapBinding
import com.example.task1921_2_24createachatappusingfirebase.model.UserData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.NotificationData
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.PushNotification
import com.example.task1921_2_24createachatappusingfirebase.notifications.entity.Token
import com.example.task1921_2_24createachatappusingfirebase.notifications.network.RetrofitInstance
import com.example.task1921_2_24createachatappusingfirebase.utils.checkInternetIsOn
import com.example.task1921_2_24createachatappusingfirebase.utils.getFileExtention
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    /** Google map */
    private lateinit var googleMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val requestCheckSettings: Int = 0x1
    private var cameraMoveJob: Job? = null
    private var receiverId: String? = null
    private var userName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(layoutInflater, container, false)
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

        /** initializing SupportMapFragment */
        mapFragment = childFragmentManager.findFragmentById(R.id.fcv_MapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        try {
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())

        } catch (e: Exception) {
            Log.e("Error", "onViewCreated: ${e.message}")
        }

        turnOnDeviceLocation()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        if (checkInternetIsOn(requireContext())) {
            googleMap = p0

            googleMap.setOnCameraMoveStartedListener {
                animateMarker(true)
            }

            googleMap.setOnCameraIdleListener {
                animateMarker(false)
            }

            try {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.night // Change to your JSON file name
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.uiSettings.isZoomControlsEnabled = false

            googleMap.setOnCameraMoveListener {
                cameraMoveJob?.cancel()
                cameraMoveJob = lifecycleScope.launch {
                    delay(500L)
                    updateLocationFromCamera()
                }
            }
            /** Get the initial location when the map is ready*/
            lifecycleScope.launch {
                getLastLocation()
            }
        } else {
            Log.e("Error", "onMapReady: Please turn on internet")
        }
    }

    private fun animateMarker(upwards: Boolean) {
        val translationY = if (upwards) -30f else 0f // Adjust the value as needed
        val animator = ObjectAnimator.ofFloat(binding.ivMarker, "translationY", translationY)
        animator.duration = 300 // Animation duration in milliseconds
        animator.interpolator =
            AccelerateDecelerateInterpolator() // Smooth acceleration and deceleration
        animator.start()
    }

    private suspend fun updateLocationFromCamera() {
        val cameraPosition = googleMap.cameraPosition
        val centerLatLong = cameraPosition.target

        // Perform geocoding to get the address
        val address = getAddressFromLocation(centerLatLong)
        address?.let { address ->
            // address value
            binding.apply {
                btnCreateGroups.setOnClickListener {
                    googleMap.snapshot { bitmap ->
                        // Create a Canvas from the bitmap to draw on it
                        bitmap?.let {
                            val canvas = Canvas(bitmap)
                            // Get the marker drawable
                            val markerDrawable =
                                ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)
                            // Calculate the marker position on the canvas
                            val markerX =
                                (bitmap.width * 0.5f) - (markerDrawable?.intrinsicWidth ?: 0) * 0.5f
                            val markerY =
                                (bitmap.height * 0.5f) - (markerDrawable?.intrinsicHeight
                                    ?: 0) * 0.5f
                            // Draw the marker on the canvas
                            markerDrawable?.setBounds(
                                markerX.toInt(),
                                markerY.toInt(),
                                (markerX + (markerDrawable.intrinsicWidth
                                    ?: 0)).toInt(),
                                (markerY + (markerDrawable.intrinsicHeight ?: 0)).toInt()
                            )
                            markerDrawable?.draw(canvas)
                        }

                        val url = getImageUriFromBitmap(requireContext(), bitmap)

                        /* navigating data to chat fragment */
                        val sharedCustomPref = requireActivity().getSharedPreferences(
                            "receiver_data",
                            FirebaseMessagingService.MODE_PRIVATE
                        ).edit()
                        sharedCustomPref.putString("MapSnapshot", url.toString())
                        sharedCustomPref.putString("LocationValue", address)
                        sharedCustomPref.apply()

                        uploadLocationUriToFirebase(
                            bitmap = url,
                            address = address,
                            lat = centerLatLong.latitude,
                            long = centerLatLong.longitude
                        )

                        Log.d("BitmapMapImage", "updateLocationFromCamera: $bitmap")
                    }
                }
            }
        }
    }

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap?): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    @Suppress("DEPRECATION")
    private suspend fun getAddressFromLocation(latLng: LatLng): String? =
        withContext(Dispatchers.IO) {
            val gcd = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? =
                gcd.getFromLocation(latLng.latitude, latLng.longitude, 1)
            return@withContext addresses?.firstOrNull()?.getAddressLine(0)
        }

    /** getting user location address from the screen center point */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val lastLocation = task.result

                /** setting camera position to user current location */
                lifecycleScope.launch(Dispatchers.Main) {
                    val initialCameraPosition = CameraPosition.builder()
                        .target(LatLng(lastLocation.latitude, lastLocation.longitude)).zoom(17f)
                        .build()

                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(initialCameraPosition))
                }
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    getLastLocation()
                }
            }
        }
    }

    /** turning on user device location */
    @Suppress("DEPRECATION")
    private fun turnOnDeviceLocation() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(10000)
        locationRequest.setFastestInterval(10000 / 2)
        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
        locationSettingsRequestBuilder.addLocationRequest(locationRequest)
        locationSettingsRequestBuilder.setAlwaysShow(true)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())
        task.addOnSuccessListener(requireActivity()) { }
        task.addOnFailureListener(requireActivity()) { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(
                        requireActivity(), requestCheckSettings
                    )
                } catch (sendIntentException: IntentSender.SendIntentException) {
                    sendIntentException.printStackTrace()
                }
            }
        }
    }


    /* upload PDF file to Firebase realtime database */
    private fun uploadLocationUriToFirebase(
        bitmap: Uri,
        address: String,
        lat: Double,
        long: Double,
    ) {
        val ref = FirebaseDatabase.getInstance().reference
        val msgKey = ref.push().key

        val fileRef =
            FirebaseStorage.getInstance().reference.child("pdf/")
                .child(
                    "${FirebaseAuth.getInstance().uid}" + "." + getFileExtention(
                        requireContext(),
                        bitmap
                    )
                )

        fileRef.putFile(bitmap)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.UK)
                        val currentDate = sdf.format(java.util.Date())

                        val msgHashMap = hashMapOf<String, Any>(
                            "sender" to FirebaseAuth.getInstance().uid!!,
                            "message" to "sent you a location.",
                            "receiver" to receiverId!!,
                            "timeStamp" to currentDate,
                            "messageId" to msgKey!!,
                            "url" to it.toString(),
                            "fileName" to address,
                            "lat" to lat,
                            "long" to long,
                            "type" to "location"
                        )
                        val chatHashMap = hashMapOf<String, Any>(
                            "receiver" to receiverId!!,
                            "isSeen" to false,
                            "timeStamp" to currentDate,
                            "url" to it.toString(),
                            "message" to "sent you a location.",
                            "fileName" to address,
                            "lat" to lat,
                            "long" to long,
                        )

                        FirebaseFirestore.getInstance().collection("Chats").document(msgKey)
                            .set(chatHashMap)

                        ref.child("Chats")
                            .child(msgKey)
                            .setValue(msgHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

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
                                                            "Location"
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