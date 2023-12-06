package com.wolf8017.twochat.model.chat

data class MessageModel(
    var uID: String? = null,
    var message: String? = null,
    var messageID: String? = null,
    var timestamp: Long? = null,
)
