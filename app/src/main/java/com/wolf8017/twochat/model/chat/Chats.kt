package com.wolf8017.twochat.model.chat

data class Chats(
    var dateTime: String? = null,
    var textMessage: String? = null,
    var type: String? = null,
    var sender: String? = null,
    var receiver: String? = null,
)
