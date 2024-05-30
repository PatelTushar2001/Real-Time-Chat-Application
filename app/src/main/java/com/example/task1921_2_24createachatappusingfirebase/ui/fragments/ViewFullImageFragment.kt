package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentViewFullImageBinding

class ViewFullImageFragment : Fragment() {
    private var _binding: FragmentViewFullImageBinding? = null
    private val binding get() = _binding!!
    var factor = 1.0f

    private val args: ViewFullImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewFullImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val scale = ScaleGestureDetector(requireContext(), MyScaleGesture(binding.ivFullImage, factor))
//        binding.ivBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//        // close fragment on swipe up/down gesture
//        view.setOnTouchListener { _, event ->
//            scale.onTouchEvent(event)
//        }
        binding.apply {
            if (args.isProfileImage) {
                ivBack.visibility = View.VISIBLE
                tvUserName.visibility = View.VISIBLE
            } else {
                ivBack.visibility = View.VISIBLE
                tvUserName.visibility = View.GONE
            }

            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }

            tvUserName.text = args.userName
        }

        Glide.with(requireContext()).load(args.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivFullImage)

    }

//    class MyScaleGesture(private val image: ImageView, private var factor: Float) : OnScaleGestureListener {
//        override fun onScale(detector: ScaleGestureDetector): Boolean {
//            factor *= detector.scaleFactor
//            factor = 0.1f.coerceAtLeast(factor.coerceAtMost(100f))
//            image.scaleX = factor
//            image.scaleY = factor
//
//            return true
//        }
//
//        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
//            return true
//        }
//
//        override fun onScaleEnd(detector: ScaleGestureDetector) {
//        }
//
//    }
}