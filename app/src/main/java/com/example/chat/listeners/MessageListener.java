package com.example.chat.listeners;

import com.example.chat.models.Message;

public interface MessageListener {
    void onHoldListener(Message message, int position);
}
