package com.example.chat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.databinding.ItemContainerUserBinding;
import com.example.chat.models.User;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private final List<User> requestList;

    public RequestAdapter(List<User> requestList) {
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding binding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RequestAdapter.RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.setRequest(requestList.get(position));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerUserBinding binding;

        public RequestViewHolder(@NonNull ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        public void setRequest(User user) {
            binding.itemContainerUserTvName.setText(user.getName());
            binding.itemContainerUserTvEmail.setText(user.getEmail());
            binding.itemContainerUserIvProfile.setImageBitmap(getUserImage(user.getImage()));
        }

    }
}
