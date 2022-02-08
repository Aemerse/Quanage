package com.aemerse.quanage.model

import androidx.annotation.Keep

@Keep
class ChatMessages {
    var url: String? = null
    var message: String? = null
    var sender: String? = null
    var timestamp: Any? = null
    var receiver: String? = null
    var type: String? = null
    var messageId: String? = null

    constructor()

    constructor(
        message: String?,
        type: String?, sender: String?, receiver: String?, timestamp: MutableMap<String, String>?,
        messageId: String?
    ) {
        this.message = message
        this.type = type
        this.sender = sender
        this.receiver = receiver
        this.timestamp = timestamp
        this.messageId = messageId
    }

    constructor(
        url: String?,
        message: String?,
        sender: String?,
        receiver: String?,
        type: String?,
        timestamp: MutableMap<String, String>?,
        messageId: String?
    ) {
        this.url = url
        this.message = message
        this.sender = sender
        this.receiver = receiver
        this.type = type
        this.timestamp = timestamp
        this.messageId = messageId
    }

}