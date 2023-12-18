package com.wolf8017.twochat.managers.interfaces

import com.wolf8017.twochat.model.chat.Chats

interface OnReadChatCallBack {
    fun onReadSuccess(list: MutableList<Chats>)
    fun onReadFailed()
}
