package com.example.chat.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.databinding.ActivityChatBinding;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;

import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getUser();
        eventHandling();
    }

    private void eventHandling() {
        binding.activityChatIvBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUser() {
        User user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.activityChatTvName.setText(Objects.requireNonNull(user).getName());
    }
}