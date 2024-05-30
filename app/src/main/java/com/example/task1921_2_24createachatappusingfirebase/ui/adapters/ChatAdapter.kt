package com.example.task1921_2_24createachatappusingfirebase.ui.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.task1921_2_24createachatappusingfirebase.R
import com.example.task1921_2_24createachatappusingfirebase.databinding.RawChatCellBinding
import com.example.task1921_2_24createachatappusingfirebase.model.ChatData
import com.example.task1921_2_24createachatappusingfirebase.model.ChatListRealData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(
    private val context: Context,
    private val chatData: ArrayList<ChatData>,
    private val onClick: (ChatData) -> Any,
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var isImage: Boolean? = null
    private var isPdf: Boolean? = null
    private var isContact: Boolean? = null
    private var isMedia: Boolean? = null
    private var isVideo: Boolean? = null
    private var isLocation: Boolean? = null

    private var isValidData: ArrayList<String> = arrayListOf()


    inner class ChatViewHolder(val binding: RawChatCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = RawChatCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = chatData.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatData[position]

        with(holder) {
            with(binding) {
                when(chat.state){
                    "received" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
//                            ivRightTickMsg.visibility = View.VISIBLE
//                            Glide.with(context).load(R.drawable.ic_done_all)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(ivRightTickMsg)
//                            ivRightTickMsg.setImageResource(R.drawable.ic_done_all)
                        } else{
                            updateUI(holder)
//                            ivLeftTickMsg.visibility = View.VISIBLE
//                            Glide.with(context).load(R.drawable.ic_done_all)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(ivLeftTickMsg)
//                            ivLeftTickMsg.setImageResource(R.drawable.ic_done_all)
                        }
                    }
                    "seen" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
//                            ivRightTickMsg.visibility = View.VISIBLE
//                            Glide.with(context).load(R.drawable.ic_check_circle)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(ivRightTickMsg)
//                            ivRightTickMsg.setImageResource(R.drawable.ic_check_circle)
                        } else{
                            updateUI(holder)
//                            ivLeftTickMsg.visibility = View.VISIBLE
//                            Glide.with(context).load(R.drawable.ic_check_circle)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(ivLeftTickMsg)
//                            ivLeftTickMsg.setImageResource(R.drawable.ic_check_circle)
                        }
                    }
                    "sent" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
//                            ivRightTickMsg.visibility = View.VISIBLE
//                            Glide.with(context).load(R.drawable.ic_done)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(ivRightTickMsg)
//                            ivRightTickMsg.setImageResource(R.drawable.ic_done)
                        } else{
                            updateUI(holder)
//                            ivLeftTickMsg.visibility = View.VISIBLE
//                            Glide.with(context).load(R.drawable.ic_done)
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(ivLeftTickMsg)
//                            ivLeftTickMsg.setImageResource(R.drawable.ic_done)
                        }
                    }
                }

                when (chat.type) {
                    "text" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            tvRightMsgText.visibility = View.VISIBLE
                            tvRightMsgTime.visibility = View.VISIBLE
//                            ivRightTickMsg.visibility = View.VISIBLE
                            tvRightMsgText.text = chat.message
                            tvRightMsgTime.text = chat.timeStamp

                        }
                        else if (!chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            tvLeftMsgText.visibility = View.VISIBLE
                            tvLeftMsgTime.visibility = View.VISIBLE
//                            ivLeftTickMsg.visibility = View.VISIBLE
                            tvLeftMsgText.text = chat.message
                            tvLeftMsgTime.text = chat.timeStamp

                        } else {
                            updateUI(holder)
                        }
                    }

                    "image" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            ivRightChatUrlImage.visibility = View.VISIBLE
                            tvRightImageTime.visibility = View.VISIBLE
                            Glide.with(context).load(chat.url)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(ivRightChatUrlImage)
                            tvRightImageTime.text = chat.timeStamp

                            // onImage click event
                            ivRightChatUrlImage.setOnClickListener { onClick.invoke(chat) }

                            ivRightChatUrlImage.setOnLongClickListener {
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
                        } else if (!chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            ivLeftChatUrlImage.visibility = View.VISIBLE
                            tvLeftImageTime.visibility = View.VISIBLE
                            Glide.with(context).load(chat.url)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(ivLeftChatUrlImage)
                            tvLeftImageTime.text = chat.timeStamp

                            // onImage click event
                            ivLeftChatUrlImage.setOnClickListener { onClick.invoke(chat) }

                            ivLeftChatUrlImage.setOnLongClickListener {
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
                        } else {
                            updateUI(holder)
                        }
                    }

                    "pdf" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvRightPdfView.visibility = View.VISIBLE
                            tvRightMsgTimePdf.visibility = View.VISIBLE
                            tvRightPdfName.text = chat.fileName
                            tvRightMsgTimePdf.text = chat.timeStamp

                            // on right pdf click event
                            cvRightPdfView.setOnClickListener {
                                onClick(chat)
                            }

                            //right pdf long click event
                            cvRightPdfView.setOnLongClickListener {
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
                            updateUI(holder)
                            cvLeftPdfView.visibility = View.VISIBLE
                            tvLeftMsgTimePdf.visibility = View.VISIBLE
                            tvLeftPdfName.text = chat.fileName
                            tvLeftMsgTimePdf.text = chat.timeStamp

                            // on left pdf click event
                            cvLeftPdfView.setOnClickListener {
                                onClick(chat)
                            }

                            //left pdf long click event
                            cvLeftPdfView.setOnLongClickListener {
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
                        } else {
                            updateUI(holder)
                        }
                    }

                    "contact" -> {

                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvRightContact.visibility = View.VISIBLE
                            tvRightMsgTimeContact.visibility = View.VISIBLE
                            tvRightContactName.text = chat.contact
                            tvRightMsgTimeContact.text = chat.timeStamp


                            // on right contact click event
                            tvRightViewAll.setOnClickListener {
                                onClick(chat)
                            }

                            //right contact long click event
                            cvRightContact.setOnLongClickListener {
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
                            updateUI(holder)
                            cvLeftContact.visibility = View.VISIBLE
                            tvLeftMsgTimeContact.visibility = View.VISIBLE
                            tvLeftContactName.text = chat.contact
                            tvLeftMsgTimeContact.text = chat.timeStamp

                            // on left contact click event
                            tvLeftViewAll.setOnClickListener {
                                onClick(chat)
                            }

                            //left contact long click event
                            cvLeftContact.setOnLongClickListener {
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
                        } else {
                            updateUI(holder)
                        }
                    }

                    "audio" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvRightAudio.visibility = View.VISIBLE
                            tvRightMsgTimeAudio.visibility = View.VISIBLE
                            tvRightAudioName.text = chat.fileName
                            tvRightMsgTimeAudio.text = chat.timeStamp


                            // on right audio click event
                            cvRightAudio.setOnClickListener {
                                onClick(chat)
                            }

                            //right audio long click event
                            cvRightAudio.setOnLongClickListener {
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

                            when (chat.fileExtention) {
                                "mp3" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_mp3
                                        )
                                    )
                                }

                                "docx" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_docx
                                        )
                                    )
                                }

                                "bin" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_bin
                                        )
                                    )
                                }

                                "pptx" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_pptx
                                        )
                                    )
                                }

                                "txt" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_txt
                                        )
                                    )
                                }

                                "apk" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_apk
                                        )
                                    )
                                }

                                "zip" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_zip
                                        )
                                    )
                                }

                                "voice" -> {
                                    ivRightAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_play
                                        )
                                    )
                                }

                                else -> {}
                            }

                        } else if (!chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvLeftAudio.visibility = View.VISIBLE
                            tvLeftMsgTimeAudio.visibility = View.VISIBLE
                            tvLeftAudioName.text = chat.fileName
                            tvLeftMsgTimeAudio.text = chat.timeStamp

                            // on left audio click event
                            cvLeftAudio.setOnClickListener {
                                onClick(chat)
                            }

                            //left audio long click event
                            cvLeftAudio.setOnLongClickListener {
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

                            when (chat.fileExtention) {
                                "mp3" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_mp3
                                        )
                                    )
                                }

                                "docx" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_docx
                                        )
                                    )
                                }

                                "bin" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_bin
                                        )
                                    )
                                }

                                "pptx" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_pptx
                                        )
                                    )
                                }

                                "txt" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_txt
                                        )
                                    )
                                }

                                "apk" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_apk
                                        )
                                    )
                                }

                                "zip" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_zip
                                        )
                                    )
                                }

                                "voice" -> {
                                    ivLeftAudio.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            context,
                                            R.drawable.ic_play
                                        )
                                    )
                                }

                                else -> {}
                            }
                        } else {
                            updateUI(holder)
                        }
                    }

                    "video" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvRightAudio.visibility = View.VISIBLE
                            tvRightMsgTimeAudio.visibility = View.VISIBLE
                            tvRightAudioName.text = chat.fileName
                            tvRightMsgTimeAudio.text = chat.timeStamp

                            ivRightAudio.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_mp4
                                )
                            )

                            // on right audio click event
                            cvRightAudio.setOnClickListener {
                                onClick(chat)
                            }

                            //right audio long click event
                            cvRightAudio.setOnLongClickListener {
                                isVideo =
                                    chat.message.equals("sent you a audio.") || chat.message.equals("sent you a media.")
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
                            updateUI(holder)
                            cvLeftAudio.visibility = View.VISIBLE
                            tvLeftMsgTimeAudio.visibility = View.VISIBLE
                            tvLeftAudioName.text = chat.fileName
                            tvLeftMsgTimeAudio.text = chat.timeStamp

                            ivLeftAudio.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_mp4
                                )
                            )

                            // on left audio click event
                            cvLeftAudio.setOnClickListener {
                                onClick(chat)
                            }

                            //left audio long click event
                            cvLeftAudio.setOnLongClickListener {
                                isVideo =
                                    chat.message.equals("sent you a audio.") || chat.message.equals("sent you a media.")
                                displayBottomSheetDialog(chat, isVideo!!, {
                                    /* on copy btn clicked */
                                    copySelectedText(chat.message!!)
                                }, {
                                    /* on Edit btn clicked */
                                    customAlertDialog(chat)
                                })
                                true
                            }
                        } else {
                            updateUI(holder)
                        }
                    }

                    "doc" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvRightAudio.visibility = View.VISIBLE
                            tvRightMsgTimeAudio.visibility = View.VISIBLE
                            tvRightAudioName.text = chat.fileName
                            tvRightMsgTimeAudio.text = chat.timeStamp

                            ivRightAudio.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_doc
                                )
                            )

                            // on right audio click event
                            cvRightAudio.setOnClickListener {
                                onClick(chat)
                            }

                            //right audio long click event
                            cvRightAudio.setOnLongClickListener {
                                isVideo =
                                    chat.message.equals("sent you a audio.") || chat.message.equals("sent you a media.")
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
                            updateUI(holder)
                            cvLeftAudio.visibility = View.VISIBLE
                            tvLeftMsgTimeAudio.visibility = View.VISIBLE
                            tvLeftAudioName.text = chat.fileName
                            tvLeftMsgTimeAudio.text = chat.timeStamp

                            ivLeftAudio.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_doc
                                )
                            )

                            // on left audio click event
                            cvLeftAudio.setOnClickListener {
                                onClick(chat)
                            }

                            //left audio long click event
                            cvLeftAudio.setOnLongClickListener {
                                isVideo =
                                    chat.message.equals("sent you a audio.") || chat.message.equals("sent you a media.")
                                displayBottomSheetDialog(chat, isVideo!!, {
                                    /* on copy btn clicked */
                                    copySelectedText(chat.message!!)
                                }, {
                                    /* on Edit btn clicked */
                                    customAlertDialog(chat)
                                })
                                true
                            }
                        } else {
                            updateUI(holder)
                        }
                    }

                    "location" -> {
                        if (chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvRightLocation.visibility = View.VISIBLE
                            tvRightLocation.text = chat.fileName
                            tvRightLocationTimePdf.text = chat.timeStamp

                            Glide.with(context).load(chat.url)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(ivRightLocation)

                            // on right audio click event
                            cvRightLocation.setOnClickListener {
                                onClick(chat)
                            }

                            //right audio long click event
                            cvRightLocation.setOnLongClickListener {
                                isLocation =
                                    chat.message.equals("sent you a location.")
                                displayBottomSheetDialog(chat, isLocation!!, {
                                    /* on copy btn clicked */
                                    copySelectedText(chat.message!!)
                                }, {
                                    /* on Edit btn clicked */
                                    customAlertDialog(chat)
                                })
                                true
                            }
                        } else if (!chat.sender.equals(firebaseUser.uid)) {
                            updateUI(holder)
                            cvLeftLocation.visibility = View.VISIBLE
                            tvLeftLocation.text = chat.fileName
                            tvLeftLocationTimePdf.text = chat.timeStamp

                            Glide.with(context).load(chat.url)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(ivLeftLocation)

                            // on left audio click event
                            cvLeftLocation.setOnClickListener {
                                onClick(chat)
                            }

                            //left audio long click event
                            cvLeftLocation.setOnLongClickListener {
                                isLocation =
                                    chat.message.equals("sent you a location.")
                                displayBottomSheetDialog(chat, isLocation!!, {
                                    /* on copy btn clicked */
                                    copySelectedText(chat.message!!)
                                }, {
                                    /* on Edit btn clicked */
                                    customAlertDialog(chat)
                                })
                                true
                            }
                        } else {
                            updateUI(holder)
                        }
                    }

                    else -> {
                        updateUI(holder)
                    }
                }

//                val unknownUserRef = FirebaseDatabase.getInstance().reference.child("ChatList")

//                unknownUserListener = unknownUserRef.child(FirebaseAuth.getInstance().uid!!)
//                    .addValueEventListener(object : ValueEventListener{
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            for (data in snapshot.children){
//                                val userDetail = data.getValue(ChatListRealData::class.java)
//                                val idValid = data.child("isValidUser").getValue(String::class.java)
//
//                                if (idValid == ""){
//                                    updateUI(holder)
//                                    clyUnknownUserOptions.visibility = View.VISIBLE
//                                    binding.tvUnknownUserName.text = String.format(context!!.getString(R.string.lbl_title_unknown_user), "chat")
//
//                                } else {
//                                    updateUI(holder)
//                                }
//
//                                Log.d("USERDETAILS", "onDataChange: ${idValid}")
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//
//                    })


                // text/image message long click event
                itemView.rootView.setOnLongClickListener {
                    /* getting isImage boolean value */
                    isImage = chat.message.equals("sent you an image.") && chat.url!!.isNotEmpty()
                    displayBottomSheetDialog(
                        chat,
                        isImage!!,
                        {
                            /* on copy btn clicked */
                            copySelectedText(chat.message!!)
                        },
                        {
                            /* on Edit btn clicked */
                            customAlertDialog(chat)
                        })

                    true
                }
            }
        }
    }

    private fun updateUI(holder: ChatViewHolder) {
        with(holder) {
            with(binding) {
                // text msg
                tvLeftMsgText.visibility = View.GONE
                tvRightMsgText.visibility = View.GONE
                tvLeftMsgTime.visibility = View.GONE
                tvRightMsgTime.visibility = View.GONE
//                ivLeftTickMsg.visibility = View.GONE
//                ivRightTickMsg.visibility = View.GONE

                // image msg
                ivLeftChatUrlImage.visibility = View.GONE
                ivRightChatUrlImage.visibility = View.GONE
                tvLeftImageTime.visibility = View.GONE
//                tvRightImageTime.visibility = View.GONE

                // pdf msg
                cvLeftPdfView.visibility = View.GONE
                cvRightPdfView.visibility = View.GONE
                tvLeftMsgTimePdf.visibility = View.GONE
//                tvRightMsgTimePdf.visibility = View.GONE

                // contact msg
                cvLeftContact.visibility = View.GONE
                cvRightContact.visibility = View.GONE
                tvRightMsgTimeContact.visibility = View.GONE
//                tvLeftMsgTimeContact.visibility = View.GONE

                //audio msg
                cvLeftAudio.visibility = View.GONE
                cvRightAudio.visibility = View.GONE
                tvRightMsgTimeAudio.visibility = View.GONE
//                tvLeftMsgTimeAudio.visibility = View.GONE

                // location

                cvLeftLocation.visibility = View.GONE
                cvRightLocation.visibility = View.GONE

                clyUnknownUserOptions.visibility = View.GONE
            }
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

    private fun getIsValidData(){
        val unknownUserRef = FirebaseDatabase.getInstance().reference.child("ChatList")

        unknownUserRef.child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children){
                        val userDetail = data.getValue(ChatListRealData::class.java)
                        val idValid = data.child("isValidUser").getValue(String::class.java)

                        isValidData.add(idValid!!)
//                        if (idValid == ""){
//                            binding.clyUnknownUserOptions.visibility = View.VISIBLE
//                            binding.rvMessages.isNestedScrollingEnabled = false
//                            binding.tvUnknownUserName.text = String.format(context!!.getString(R.string.lbl_title_unknown_user), args.userData.userName)
//                        } else {
//                            binding.clyUnknownUserOptions.visibility = View.GONE
//                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}