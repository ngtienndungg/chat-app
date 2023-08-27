package com.example.chat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.databinding.ItemContainerUserBinding;
import com.example.chat.listeners.FriendListener;
import com.example.chat.models.User;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.UserViewHolder> {
    private final List<User> friends;
    private final FriendListener friendListener;

    public FriendAdapter(List<User> friends, FriendListener friendListener) {
        this.friends = friends;
        this.friendListener = friendListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding binding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setFriend(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerUserBinding binding;

        public UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        private void setFriend(User user) {
            binding.itemContainerUserTvName.setText(user.getName());
            binding.itemContainerUserTvEmail.setText(user.getEmail());
            binding.itemContainerUserIvProfile.setImageBitmap(getUserImage(user.getImage()));
            binding.getRoot().setOnClickListener(v -> friendListener.onUserClicked(user));
        }
    }
}
