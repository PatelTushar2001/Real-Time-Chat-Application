package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.model.ChatData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ChatsAdapter(
    private val context: Context,
    private val chatData: ArrayList<ChatData>,
    private val onClick: (ChatData) -> Any,
) :
    RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var isImage: Boolean? = null
    private var isPdf: Boolean? = null
    private var isContact: Boolean? = null
    private var isMedia: Boolean? = null
    private var isVideo: Boolean? = null

    inner class ChatViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {
        //text
        val showTextMsg: TextView = binding.findViewById(R.id.tv_MsgText)

        //image
        val leftImageView = binding.findViewById<ImageView>(R.id.iv_LeftChatUrlImage)
        val rightImageView = binding.findViewById<ImageView>(R.id.iv_RightChatUrlImage)

        //pdf
        val leftPdfView = binding.findViewById<CardView>(R.id.cv_LeftPdf)
        val rightPdfView = binding.findViewById<CardView>(R.id.cv_RightPdf)
        val leftPdfName = binding.findViewById<TextView>(R.id.tv_LeftPdfName)
        val rightPdfName = binding.findViewById<TextView>(R.id.tv_RightPdfName)


        //contact
        val leftContactView = binding.findViewById<CardView>(R.id.cv_LeftContact)
        val rightContactView = binding.findViewById<CardView>(R.id.cv_RightContact)
        val leftContactName = binding.findViewById<TextView>(R.id.tv_LeftContactName)
        val rightContactName = binding.findViewById<TextView>(R.id.tv_RightContactName)


        //audio
        val leftAudioView = binding.findViewById<CardView>(R.id.cv_LeftAudio)
        val rightAudioView = binding.findViewById<CardView>(R.id.cv_RightAudio)
        val leftAudioName = binding.findViewById<TextView>(R.id.tv_LeftAudioName)
        val rightAudioName = binding.findViewById<TextView>(R.id.tv_RightAudioName)
        val leftAudioImageView = binding.findViewById<ImageView>(R.id.iv_LeftAudio)
        val rightAudioImageView = binding.findViewById<ImageView>(R.id.iv_RightAudio)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsAdapter.ChatViewHolder {
        return if (viewType == 1) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.raw_right_chatlist_cell, parent, false)
            ChatViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.raw_left_chatlist_cell, parent, false)
            ChatViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ChatsAdapter.ChatViewHolder, position: Int) {
        val chat = chatData[position]

        try {


            if (chat.message.equals("sent you an image.") && chat.url != "") {
                if (chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.rightPdfView.visibility = View.GONE
                    holder.rightContactView.visibility = View.GONE
                    holder.rightAudioView.visibility = View.GONE
                    holder.rightImageView.visibility = View.VISIBLE

                    Glide.with(context).load(chat.url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.rightImageView)
                    holder.itemView.setOnClickListener { onClick.invoke(chat) }
                } else if (!chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.leftPdfView.visibility = View.GONE
                    holder.leftContactView.visibility = View.GONE
                    holder.leftAudioView.visibility = View.GONE
                    holder.leftImageView.visibility = View.VISIBLE
//
                    Glide.with(context).load(chat.url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.leftImageView)
                    holder.itemView.setOnClickListener { onClick.invoke(chat) }
                }
            } else if (chat.message.equals("sent you a pdf.") && chat.url != "") {
                if (chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.rightContactView.visibility = View.GONE
                    holder.rightAudioView.visibility = View.GONE
                    holder.rightPdfView.visibility = View.VISIBLE
                    holder.rightPdfName.text = chat.fileName

                    holder.rightPdfView.setOnClickListener {
                        onClick(chat)
                    }
                    holder.rightPdfView.setOnLongClickListener {
                        isPdf = chat.message.equals("sent you a pdf.") && chat.url!!.isNotEmpty()
                        displayBottomSheetDialog(chat, isPdf!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }

                } else if (!chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.leftImageView.visibility = View.GONE
                    holder.leftContactView.visibility = View.GONE
                    holder.leftAudioView.visibility = View.GONE
                    holder.leftPdfView.visibility = View.VISIBLE
                    holder.leftPdfName.text = chat.fileName

                    holder.leftPdfView.setOnClickListener {
                        onClick(chat)
                    }
                    holder.leftPdfView.setOnLongClickListener {
                        isPdf = chat.message.equals("sent you a pdf.") && chat.url!!.isNotEmpty()
                        displayBottomSheetDialog(chat, isPdf!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }
                }
            } else if (chat.message.equals("sent contacts.") && chat.contact!!.isNotEmpty()) {
                if (chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.rightPdfView.visibility = View.GONE
                    holder.rightImageView.visibility = View.GONE
                    holder.rightAudioView.visibility = View.GONE
                    holder.rightContactView.visibility = View.VISIBLE
                    holder.rightContactName.text = chat.contact

                    holder.rightContactView.setOnClickListener { onClick.invoke(chat) }
                    holder.rightContactView.setOnLongClickListener {
                        isContact = chat.message.equals("sent contacts.")
                        displayBottomSheetDialog(chat, isContact!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }
                } else if (!chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.leftPdfView.visibility = View.GONE
                    holder.leftImageView.visibility = View.GONE
                    holder.leftAudioView.visibility = View.GONE
                    holder.leftContactView.visibility = View.VISIBLE
                    holder.leftContactName.text = chat.contact

                    holder.leftContactView.setOnClickListener { onClick.invoke(chat) }
                    holder.leftContactView.setOnLongClickListener {
                        isContact = chat.message.equals("sent contacts.")
                        displayBottomSheetDialog(chat, isContact!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }
                }
            } else if (chat.message.equals("sent you a audio.") && chat.url!!.isNotEmpty() || chat.message.equals(
                    "sent you a media."
                ) && chat.url!!.isNotEmpty()
            ) {
                if (chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.rightPdfView.visibility = View.GONE
                    holder.rightImageView.visibility = View.GONE
                    holder.rightContactView.visibility = View.GONE
                    holder.rightAudioView.visibility = View.VISIBLE
                    holder.rightAudioName.text = chat.fileName

                    when (chat.fileExtention) {
                        "mp3" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_mp3
                                )
                            )
                        }

                        "docx" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_docx
                                )
                            )
                        }

                        "bin" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_bin
                                )
                            )
                        }

                        "pptx" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_pptx
                                )
                            )
                        }

                        "txt" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_txt
                                )
                            )
                        }

                        "apk" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_apk
                                )
                            )
                        }

                        "zip" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_zip
                                )
                            )
                        }

                        "voice" -> {
                            holder.rightAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_play
                                )
                            )
                        }
                    }

                    holder.rightAudioView.setOnClickListener { onClick.invoke(chat) }
                    holder.rightAudioView.setOnLongClickListener {
                        isMedia =
                            chat.message.equals("sent you a audio.") || chat.message.equals("sent you a media.")
                        displayBottomSheetDialog(chat, isMedia!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }
                } else if (!chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.leftPdfView.visibility = View.GONE
                    holder.leftImageView.visibility = View.GONE
                    holder.leftContactView.visibility = View.GONE
                    holder.leftAudioView.visibility = View.VISIBLE
                    holder.leftAudioName.text = chat.fileName

                    when (chat.fileExtention) {
                        "mp3" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_mp3
                                )
                            )
                        }

                        "docx" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_docx
                                )
                            )
                        }

                        "bin" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_bin
                                )
                            )
                        }

                        "pptx" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_pptx
                                )
                            )
                        }

                        "txt" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_txt
                                )
                            )
                        }

                        "apk" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_apk
                                )
                            )
                        }

                        "zip" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_zip
                                )
                            )
                        }

                        "voice" -> {
                            holder.leftAudioImageView.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_play
                                )
                            )
                        }
                    }

                    holder.leftAudioView.setOnClickListener { onClick.invoke(chat) }
                    holder.leftAudioView.setOnLongClickListener {
                        isMedia =
                            chat.message.equals("sent you a audio.") || chat.message.equals("sent you a media.")
                        displayBottomSheetDialog(chat, isMedia!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }
                }
            } else if (chat.message.equals("sent you a video.") && chat.url!!.isNotEmpty()) {
                if (chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.rightPdfView.visibility = View.GONE
                    holder.rightImageView.visibility = View.GONE
                    holder.rightContactView.visibility = View.GONE
                    holder.rightAudioView.visibility = View.VISIBLE
                    holder.rightAudioName.text = chat.fileName
                    holder.rightAudioImageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_mp4
                        )
                    )

                    holder.rightAudioView.setOnClickListener { onClick.invoke(chat) }
                    holder.rightAudioView.setOnLongClickListener {
                        isVideo = chat.message.equals("sent you a video.")
                        displayBottomSheetDialog(chat, isVideo!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }
                } else if (!chat.sender.equals(firebaseUser.uid)) {
                    holder.showTextMsg.visibility = View.GONE
                    holder.leftPdfView.visibility = View.GONE
                    holder.leftImageView.visibility = View.GONE
                    holder.leftContactView.visibility = View.GONE
                    holder.leftAudioView.visibility = View.VISIBLE
                    holder.leftAudioName.text = chat.fileName
                    holder.leftAudioImageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_mp4
                        )
                    )

                    holder.leftAudioView.setOnClickListener { onClick.invoke(chat) }
                    holder.leftAudioView.setOnLongClickListener {
                        isVideo = chat.message.equals("sent you a video.")
                        displayBottomSheetDialog(chat, isVideo!!, {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        }, {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })
                        true
                    }
                }
            } else {
//                holder.leftPdfView.visibility = View.GONE
//                holder.leftImageView.visibility = View.GONE
//                holder.leftContactView.visibility = View.GONE
//                holder.leftAudioView.visibility = View.GONE
//                holder.rightPdfView.visibility = View.GONE
//                holder.rightImageView.visibility = View.GONE
//                holder.rightContactView.visibility = View.GONE
//                holder.rightAudioView.visibility = View.GONE
                updateUI(holder)
                holder.showTextMsg.text = chat.message
            }
        } catch (e: Exception) {
            Log.e("ErorrLoadingChatData", "onBindViewHolder: ${e.message}")
        }

        holder.itemView.rootView.setOnLongClickListener {
            /* getting isImage boolean value */
            isImage = chat.message.equals("sent you an image.") && chat.url!!.isNotEmpty()
            displayBottomSheetDialog(chat, isImage!!, {
                /* on copy btn clicked */
                copySelectedText(chat.message!!)
            }, {
                /* on Edit btn clicked */
                customAlertDialog(chat)
            })

            true
        }
    }

    override fun getItemCount(): Int = chatData.size

    override fun getItemViewType(position: Int): Int {
        return if (chatData[position].sender == firebaseUser.uid) {
            1
        } else {
            0
        }
    }

    /* custom bottom sheet dialog */
    @SuppressLint("InflateParams")
    private fun displayBottomSheetDialog(
        chatData: ChatData,
        isImage: Boolean,
        onCopyClick: () -> Any,
        onEditClick: () -> Any
    ) {
        val dialog = BottomSheetDialog(context, R.style.Theme_BottomSheetTheme)
        val customView = LayoutInflater.from(context).inflate(R.layout.custom_btm_dialog, null)

        val btnClose = customView.findViewById<AppCompatButton>(R.id.btn_Cancel)
        val copy = customView.findViewById<ImageButton>(R.id.iv_Copy)
        val copyText = customView.findViewById<AppCompatTextView>(R.id.tv_Copy)
        val edit = customView.findViewById<ImageButton>(R.id.iv_Edit)
        val editTxt = customView.findViewById<AppCompatTextView>(R.id.tv_Edit)
        val delete = customView.findViewById<ImageButton>(R.id.iv_Delete)

        if (isImage) {
            edit.visibility = View.GONE
            editTxt.visibility = View.GONE
            copy.visibility = View.GONE
            copyText.visibility = View.GONE
        } else {
            edit.visibility = View.VISIBLE
            editTxt.visibility = View.VISIBLE
            copy.visibility = View.VISIBLE
            copyText.visibility = View.VISIBLE
        }

        /* copy chat data */
        copy.setOnClickListener {
            onCopyClick()
            dialog.dismiss()
        }

        /* edit chat on edit btn click event */
        edit.setOnClickListener {
            onEditClick()
            dialog.dismiss()
        }

        /* deleting chat on delete btn click event */
        delete.setOnClickListener {
            FirebaseFirestore.getInstance().collection("Chats").document(chatData.messageId!!)
                .delete()

            FirebaseDatabase.getInstance().reference.child("Chats")
                .child(chatData.messageId)
                .removeValue()

            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.setContentView(customView)
        dialog.show()
    }

    /* copy selected text of chat */
    private fun copySelectedText(text: String) {
        val clipBoardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("text", text)
        clipBoardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_LONG).show()
    }

    /* custom Alert dialog */
    private fun customAlertDialog(chatMessage: ChatData) {
        val dialogBuilder = AlertDialog.Builder(context, R.style.Theme_BottomSheetTheme)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog_lyt, null)
        dialogBuilder.setView(view)
        val btnEdit = view.findViewById<AppCompatButton>(R.id.btn_EditChanges)
        val edtEditMessage = view.findViewById<AppCompatEditText>(R.id.edt_MessageEdit)

        edtEditMessage.setText(chatMessage.message)

        val alert = dialogBuilder.create()
        alert.setCancelable(true)
        alert.show()

        /* updating chat data on btnEdit click event */
        btnEdit.setOnClickListener {
            if (edtEditMessage.text!!.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("Chats")
                    .document(chatMessage.messageId!!)
                    .update("message", edtEditMessage.text.toString())

                val updateChatMsg = hashMapOf<String, Any>(
                    "message" to edtEditMessage.text.toString()
                )
                FirebaseDatabase.getInstance().reference.child("Chats").child(chatMessage.messageId)
                    .updateChildren(updateChatMsg)
                alert.dismiss()
            } else {
                edtEditMessage.error = "Message Value should not be empty"
            }
        }
    }

    private fun updateUI(holder: ChatsAdapter.ChatViewHolder) {

        holder.rightPdfView.visibility = View.GONE
        holder.rightContactView.visibility = View.GONE
        holder.rightAudioView.visibility = View.GONE
        holder.rightImageView.visibility = View.GONE

        holder.leftImageView.visibility = View.GONE
        holder.leftContactView.visibility = View.GONE
        holder.leftAudioView.visibility = View.GONE
        holder.leftPdfView.visibility = View.GONE

    }
}
