package com.example.chat.listeners;

import com.example.chat.models.User;

public interface RequestListener {
    void onAcceptClick(User user, int position);
    void onDenyClick(User user);
}
