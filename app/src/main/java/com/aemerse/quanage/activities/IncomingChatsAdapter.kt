package com.aemerse.quanage.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aemerse.quanage.R
import com.aemerse.quanage.model.ChatMessages
import com.aemerse.quanage.model.User
import com.aemerse.quanage.utils.loadImageWithoutTransition
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class IncomingChatsAdapter(private val context: Context, private val chats: List<ChatMessages>, private val mUserList: ArrayList<User>, private val listener: ItemClickListener) : RecyclerView.Adapter<IncomingChatsAdapter.ViewHolder>() {

    private var userName: String? = null
    private var chatMessages: ChatMessages? = null
    private var v: View? = null
    private var timeString: String? = null
    private var dateString: String? = null
    private var oldDate: String? = null

    override fun getItemViewType(position: Int): Int {
        chatMessages = chats[position]
        return if (chatMessages!!.sender == Firebase.auth.currentUser!!.uid) {
            1
        } else {
            2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            2 -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.incoming_chats, parent, false)
            }
            1 -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.chats_me, parent, false)
            }
        }
        return ViewHolder(v!!, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        chatMessages = chats[position]
        if (holder.itemViewType == 2) {
            for (user in mUserList) {
                if (user.userId == chatMessages!!.sender) {
                    userName = user.username
                    holder.name!!.text = userName
                    break
                }
            }
        }
        if(chatMessages!!.type=="text"){
            holder.image.visibility = GONE
            holder.incomingChats.text = chatMessages!!.message
            holder.incomingChats.visibility = VISIBLE
        }
        else{
            holder.image.visibility = VISIBLE
            loadImageWithoutTransition(context,chatMessages!!.url,holder.image)
            if(chatMessages!!.message==null){
                holder.incomingChats.visibility = GONE
            }
        }

        try {
            val date = chatMessages!!.timestamp as Long
            timeString = SimpleDateFormat("h:mm a", Locale.ENGLISH).format(Date(date))
            dateString = SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH).format(Date(date))
            holder.time.text = timeString
            holder.dateText.text = dateString

//            if(dateString==oldDate){
//                holder.dateText.visibility = GONE
//            }
//            else{
//                holder.dateText.visibility = VISIBLE
//                oldDate = dateString
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    inner class ViewHolder(itemView: View, listener: ItemClickListener) :
        RecyclerView.ViewHolder(itemView), OnLongClickListener, OnClickListener {

        val time: TextView = itemView.findViewById(R.id.time)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val incomingChats: TextView = itemView.findViewById(R.id.incomingChat)
        val image: ImageView = itemView.findViewById(R.id.image)
        val rel: RelativeLayout = itemView.findViewById(R.id.rel)
        var name:TextView? = null

        private val clickListener: ItemClickListener = listener

        override fun onLongClick(v: View): Boolean {
            clickListener.onItemLongClicked(adapterPosition)
            return false
        }

        init {
            rel.setOnLongClickListener(this)
            image.setOnClickListener(this)
            if (chatMessages!!.sender != Firebase.auth.currentUser!!.uid) {
                name = itemView.findViewById(R.id.name)
            }
        }

        override fun onClick(v: View?) {
            clickListener.onItemSingleClicked(adapterPosition)
        }
    }

    interface ItemClickListener {
        fun onItemLongClicked(position: Int)
        fun onItemSingleClicked(position: Int)
    }

}