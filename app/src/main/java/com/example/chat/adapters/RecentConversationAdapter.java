package com.example.chat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.databinding.ItemContainerRecentConversationBinding;
import com.example.chat.listeners.ChatListener;
import com.example.chat.models.Message;
import com.example.chat.models.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.RecentConversationViewHolder> {
    private final List<Message> messages;
    private final ChatListener chatListener;

    public RecentConversationAdapter(List<Message> messages, ChatListener chatListener) {
        this.messages = messages;
        this.chatListener = chatListener;
    }

    @NonNull
    @Override
    public RecentConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentConversationViewHolder(ItemContainerRecentConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversationViewHolder holder, int position) {
        holder.setData(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private Bitmap getProfileImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public class RecentConversationViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversationBinding binding;

        public RecentConversationViewHolder(ItemContainerRecentConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(Message message) {
            binding.itemContainerUserIvProfile.setImageBitmap(getProfileImage(message.getConversationImage()));
            binding.itemContainerUserTvName.setText(message.getConversationName());
            binding.itemContainerUserTvRecentMessage.setText(message.getMessageContent());
            binding.getRoot().setOnClickListener(v -> {
                User receivedUser = new User();
                receivedUser.setId(message.getConversationId());
                receivedUser.setName(message.getConversationName());
                receivedUser.setImage(message.getConversationImage());
                chatListener.onRecentConversationClicked(receivedUser);
            });
        }
    }
}
