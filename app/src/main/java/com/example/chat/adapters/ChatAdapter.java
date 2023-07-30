package com.example.chat.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.databinding.ItemContainerReceivedMessageBinding;
import com.example.chat.databinding.ItemContainterSentMessageBinding;
import com.example.chat.models.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Bitmap receiverProfileImage;
    private final List<Message> messages;
    private final String senderId;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVE = 0;

    public ChatAdapter(Bitmap receiverProfileImage, List<Message> messages, String senderId) {
        this.receiverProfileImage = receiverProfileImage;
        this.messages = messages;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new SentMessageViewHolder(ItemContainterSentMessageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false));
        } else {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(messages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(messages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVE;
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainterSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainterSentMessageBinding itemContainterSentMessageBinding) {
            super(itemContainterSentMessageBinding.getRoot());
            binding = itemContainterSentMessageBinding;
        }

        public void setData(Message message) {
            binding.itemContainerSentMessageTvMessage.setText(message.getMessageContent());
            binding.itemContainerSentMessageTvDatetime.setText(message.getMessageContent());
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        public void setData(Message message, Bitmap profileImage) {
            binding.itemContainerReceivedMessageIvProfile.setImageBitmap(profileImage);
            binding.itemContainerReceivedMessageTvMessage.setText(message.getMessageContent());
            binding.itemContainerReceivedMessageTvMessage.setText(message.getDateTime());
        }
    }


}
