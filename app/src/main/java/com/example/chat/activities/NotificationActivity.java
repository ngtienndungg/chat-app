package com.example.chat.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.databinding.ActivityNotificationBinding;

public class NotificationActivity extends AppCompatActivity {
    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityNotificationBtLogin.setOnClickListener(v ->
                startActivity(new Intent(NotificationActivity.this, LoginActivity.class)));
    }

    @Override
    public void onBackPressed() {
        binding.activityNotificationBtLogin.setOnClickListener(v ->
                startActivity(new Intent(NotificationActivity.this, LoginActivity.class)));
    }
}