package com.example.task1921_2_24createachatappusingfirebase.ui.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.FragmentPdfViewBinding
import com.github.barteksc.pdfviewer.PDFView
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfViewFragment : Fragment() {
    private var _binding: FragmentPdfViewBinding? = null
    private val binding get() = _binding!!

    private val args: PdfViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled", "ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            tvPdfName.text = args.chatData.fileName

//            wvMain.webViewClient = WebViewClient()
//            wvMain.loadUrl("https://docs.google.com/gview?embedded=true&url=https://firebasestorage.googleapis.com/v0/b/task-chat-app-88822.appspot.com/o/pdf%2FoGLW5u9QQwMgYkBqk85V5TyW55V2.pdf?alt=media&token=61cec6ba-cc1c-4876-853b-fe209c3e29ba")
            wvMain.settings.javaScriptEnabled = true
            wvMain.webChromeClient = WebChromeClient()
            wvMain.webViewClient = WebViewClient()

//            wvMain.setBackgroundColor(R.color.transperent)

            /* display PDF file */
            RetrivePdfStream(binding.pvPdfView).execute(args.chatData.url)
        }
    }

    override fun onStart() {
        super.onStart()
//        binding.wvMain.loadUrl(args.chatData.url.toString())
        val file =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/Timetable.pdf" // file:///storage/emulated/0/Documents/Timetable.pdf
        val url = File(file)
        val uri = Uri.fromFile(url)
        val final = Uri.fromFile(File(uri.toString()))
        binding.wvMain.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=${final}")
    }

    class RetrivePdfStream(pdfView: PDFView) : AsyncTask<String, Void, InputStream>() {
        @SuppressLint("StaticFieldLeak")
        var pdf = pdfView
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String?): InputStream {
            var inputStream: InputStream? = null
            try {
                val url = URL(params[0])
                val urlConnection = url.openConnection() as HttpURLConnection

                if (urlConnection.responseCode == 200) {
                    inputStream = BufferedInputStream(urlConnection.inputStream)
                }
            } catch (e: Exception) {
                Log.e("Error", "doInBackground: ${e.message}")
            }
            return inputStream!!
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: InputStream?) {
            pdf.fromStream(result).load()
        }
    }
}