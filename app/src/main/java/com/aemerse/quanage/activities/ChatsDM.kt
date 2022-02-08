package com.aemerse.quanage.activities

import android.Manifest
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.aemerse.dazzle.Dazzle
import com.aemerse.dazzle.utils.DazzleOptions
import com.aemerse.quanage.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.aemerse.quanage.databinding.ChatsDmActivityBinding
import com.aemerse.quanage.model.ChatMessages
import com.aemerse.quanage.model.User
import com.aemerse.quanage.utils.*
import com.aemerse.quanage.utils.toast
import com.google.common.base.Strings
import com.google.firebase.database.*
import com.vanniktech.emoji.EmojiPopup
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ChatsDM : AppCompatActivity(), IncomingChatsAdapter.ItemClickListener {

    private val chats = ArrayList<ChatMessages>()
    private val mUserList = ArrayList<User>()
    private var userId: String? = null
    private var phoneNumber: String? = null
    private var returnValue: ArrayList<String>? = ArrayList()
    private lateinit var binding: ChatsDmActivityBinding
    private var snapshotListener: Query? = null
    private var count = 0
    private var messagesListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatsDmActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = Firebase.auth.currentUser?.uid

        binding.toolbarTop.setNavigationOnClickListener { onBackPressed() }

        binding.cancelButton.setOnClickListener {
            binding.replyLayout.visibility = GONE
        }

        val emojiPopup = EmojiPopup.Builder.fromRootView(binding.root)
            .setOnSoftKeyboardCloseListener {
                binding.toolbarBottom.setNavigationIcon(R.drawable.ic_emoji)
            }
            .build(binding.editText)

        binding.toolbarTop.setOnMenuItemClickListener { item: MenuItem ->
            val idTop: Int = item.itemId
            if (idTop == R.id.call) {
                if(!askForPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), permissionsRequest)){
                    MaterialAlertDialogBuilder(this,R.style.BottomSheet)
                        .setTitle("Call?")
                        .setMessage("Right away cellular call them")
                        .setPositiveButton(R.string.proceed) { dialog, which ->
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.data = Uri.parse("tel:$phoneNumber")
                            startActivity(intent)
                        }
                        .setNeutralButton(R.string.cancel, null)
                        .setIcon(R.drawable.ic_phone_call)
                        .show()
                }
            }
            else if (idTop == R.id.options) {
                val options: Array<String> =
                    arrayOf("Group Media", "Search", "Leave")
                MaterialAlertDialogBuilder(this@ChatsDM,R.style.BottomSheet)
                    .setTitle("Options")
                    .setItems(options) { dialog, which -> }
                    .show()
            }
            false
        }
        binding.toolbarBottom.setNavigationOnClickListener {
            binding.chatsRecycler.scrollToPosition(chats.size - 1)
            if (!emojiPopup.isShowing) {
                binding.toolbarBottom.setNavigationIcon(R.drawable.ic_keyboard_24dp)
                emojiPopup.toggle()
            } else {
                binding.toolbarBottom.setNavigationIcon(R.drawable.ic_emoji)
                emojiPopup.dismiss()
            }
        }
        binding.toolbarBottom.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.send -> {
                    val text: String = binding.editText.text.toString().trim()
                    if (!Strings.isNullOrEmpty(text)) {
                        binding.replyLayout.visibility = GONE
                        binding.editText.text = null
                        val chatmessages = Firebase.database("https://quanage-f2ca9-default-rtdb.firebaseio.com/").reference
                            .child("/1on1chats/${firebaseKey(userId!!)}/")
                            .push()
                        val messageId = chatmessages.key
                        val chatMessages = ChatMessages(
                            text, "text",
                            Firebase.auth.currentUser!!.uid, userId,
                            ServerValue.TIMESTAMP,
                            messageId
                        )
                        chatmessages.setValue(chatMessages)
                    } else {
                        if (count % 2 == 0) {
                            binding.chatsRecycler.smoothScrollToPosition(0)
                        } else {
                            binding.chatsRecycler.smoothScrollToPosition(chats.size - 1)
                        }
                        count++
                    }
                }
                R.id.camera -> {
                    Dazzle.startPicker(
                        this@ChatsDM, DazzleOptions.init()
                    )
                }
                R.id.record -> {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    intent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
                    intent.putExtra(
                        RecognizerIntent.EXTRA_PROMPT,
                        "Say something!"
                    )
                    intent.putExtra("android.speech.extra.DICTATION_MODE", true)
                    try {
                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
                    } catch (a: ActivityNotFoundException) {
                        toast("Speech_not_supported")
                    }
                }
            }
            false
        }

        binding.chatsRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = IncomingChatsAdapter(this, chats, mUserList, this)
        binding.chatsRecycler.adapter = adapter


        snapshotListener = Firebase.database("https://quanage-f2ca9-default-rtdb.firebaseio.com/").reference
            .child("/1on1chats/${firebaseKey(userId!!)}")
            .orderByChild("timestamp")
        messagesListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chats.clear()
                adapter.notifyDataSetChanged()
                for (doc in snapshot.children) {
                    chats.add(doc.getValue(ChatMessages::class.java)!!)
                    adapter.notifyItemInserted(chats.size-1)
                    binding.chatsRecycler.scrollToPosition(chats.size - 1)
                }
                binding.animationView.visibility = GONE
            }

            override fun onCancelled(error: DatabaseError) {}

        }
        snapshotListener!!.addValueEventListener(messagesListener!!)

        Firebase.firestore.collection("Users").document((userId)!!)[Source.CACHE]
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                if (documentSnapshot != null) {
                    val user: User? = documentSnapshot.toObject(User::class.java)
                    phoneNumber = user!!.phoneNumber
                    binding.toolbarTop.title = user.username

                    mUserList.add(user)
                }
            }

        val messageSwipeController = MessageSwipeController(this, object : SwipeControllerActions {
            override fun showReplyUI(position: Int) {
                showQuotedMessage(chats[position])
            }
        })

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.chatsRecycler)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data!=null) {
            when (requestCode) {
                REQ_CODE_SPEECH_INPUT -> {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val existingText = binding.editText.text.toString() + " " + result!![0]
                    binding.editText.setText(existingText)
                }
                Dazzle.REQUEST_CODE_PICKER -> {
                    returnValue = data.getStringArrayListExtra(Dazzle.PICKED_MEDIA_LIST)
                    val intent = Intent(this, ImagesAndVideosActivity::class.java)
                    intent.putStringArrayListExtra("selections", returnValue)
                    startActivityForResult(intent, REQ_SELECTION)
                }
                REQ_SELECTION -> {
                    returnValue = data.getStringArrayListExtra("selections")
                    uploadSelections()
                }
            }
        }
    }

    private fun uploadSelections() {
        returnValue?.map { s ->
            if(isVideo(s)){
                val file = File(
                    getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                    "VID_" + System.currentTimeMillis().toString() + ".mp4"
                )
                val firestoreRefHere = Firebase.database("https://quanage-f2ca9-default-rtdb.firebaseio.com/").reference
                    .child("/1on1chats/${firebaseKey(userId!!)}")
                    .push()
                val messageId = firestoreRefHere.key

                GlobalScope.launch(Dispatchers.Default) {
                    VideoCompressor.start(
                        s,
                        file.path,
                        object : CompressionListener {
                            override fun onProgress(percent: Float) {}

                            override fun onStart() {}

                            override fun onSuccess() {
                                Firebase.storage.reference
                                    .child("/1on1chats/${firebaseKey(userId!!)}/$messageId")
                                    .putFile(Uri.fromFile(file))
                                    .addOnSuccessListener { taskSnapshot ->
                                        taskSnapshot.metadata!!.reference!!.downloadUrl
                                            .addOnSuccessListener { uri: Uri ->
                                                val chatMessages = ChatMessages(
                                                    uri.toString(),
                                                    null,
                                                    Firebase.auth.currentUser!!.uid,
                                                    userId,
                                                    "video",
                                                    ServerValue.TIMESTAMP,
                                                    messageId
                                                )
                                                firestoreRefHere.setValue(chatMessages)
                                            }
                                    }
                            }

                            override fun onFailure(failureMessage: String) {
                                Firebase.storage.reference
                                    .child("/1on1chats/${firebaseKey(userId!!)}/${messageId}")
                                    .putFile(Uri.fromFile(File(s)))
                                    .addOnSuccessListener { taskSnapshot ->
                                        taskSnapshot.metadata!!.reference!!.downloadUrl
                                            .addOnSuccessListener { uri: Uri ->
                                                val chatMessages = ChatMessages(
                                                    uri.toString(),
                                                    null,
                                                    Firebase.auth.currentUser!!.uid,
                                                    userId,
                                                    "video",
                                                    ServerValue.TIMESTAMP,
                                                    messageId
                                                    )
                                                firestoreRefHere.setValue(chatMessages)
                                            }
                                    }
                            }

                            override fun onCancelled() {
                                Log.wtf("TAG", "compression has been cancelled")
                                // make UI changes, cleanup, etc
                            }
                        },
                        VideoQuality.MEDIUM,
                        isMinBitRateEnabled = true,
                        keepOriginalResolution = false,
                    )
                }
            } else{
                GlobalScope.launch(Dispatchers.Default) {
                    val firestoreRefHere = Firebase.database("https://quanage-f2ca9-default-rtdb.firebaseio.com/").reference
                        .child("/1on1chats/${firebaseKey(userId!!)}")
                        .push()
                    val messageId = firestoreRefHere.key

                    Firebase.storage.reference
                        .child("/1on1chats/${firebaseKey(userId!!)}/${messageId}")
                        .putFile(Uri.fromFile(Compressor.compress(applicationContext, File(s))))
                        .addOnSuccessListener { taskSnapshot ->
                            taskSnapshot.metadata!!.reference!!.downloadUrl
                                .addOnSuccessListener { uri: Uri ->
                                    val chatMessages = ChatMessages(
                                        uri.toString(),
                                        null, Firebase.auth.currentUser!!.uid,
                                        userId,
                                        "image",
                                        ServerValue.TIMESTAMP,
                                        messageId
                                        )
                                    firestoreRefHere.setValue(chatMessages)
                                }
                        }
                }
            }
        }
    }

    private fun showQuotedMessage(message: ChatMessages) {
        binding.editText.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput( binding.editText, InputMethodManager.SHOW_IMPLICIT)
        binding.txtQuotedMsg.text = message.message
        binding.replyLayout.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.let { snapshotListener?.removeEventListener(it) }
    }

    override fun onBackPressed() {
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        super.onBackPressed()
    }

    override fun onItemLongClicked(position: Int) {
        val message = chats[position]
        if ((message.sender ==  Firebase.auth.currentUser!!.uid)) {
            MaterialAlertDialogBuilder(this,R.style.BottomSheet)
                .setTitle("Delete Message?")
                .setMessage("Message will be deleted from the chat")
                .setPositiveButton(R.string.proceed) { dialog, _ ->
                    Firebase.database("https://quanage-f2ca9-default-rtdb.firebaseio.com/").reference
                        .child("/1on1chats/${firebaseKey(userId!!)}/${message.messageId}")
                        .removeValue()
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.cancel, null)
                .setIcon(R.drawable.ic_trash)
                .show()
        } else {
            MaterialAlertDialogBuilder(this,R.style.BottomSheet)
                .setTitle("Copy Message?")
                .setMessage("Message will be saved to the clipboard")
                .setPositiveButton(R.string.proceed) { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", message.message)
                    clipboard.setPrimaryClip(clip)
                }
                .setNeutralButton(R.string.cancel, null)
                .setIcon(R.drawable.ic_copy)
                .show()
        }
    }

    override fun onItemSingleClicked(position: Int) {
        val message = chats[position]
        val intent = Intent(this, SelectedActivity::class.java)
        if(message.type=="video"){
            intent.putExtra("type", message.type)
        }
        intent.putExtra("url", message.url)
        startActivity(intent)
    }
}