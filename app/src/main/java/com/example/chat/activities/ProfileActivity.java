package com.example.chat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.databinding.ActivityProfileBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initiate();
        eventHandling();
    }

    private void initiate() {
        binding.activityRegisterIvProfile.setImageBitmap(getProfileImage(new PreferenceManager(this).getData(Constants.KEY_IMAGE)));
        binding.activityRegisterEtInputName.setText(new PreferenceManager(this).getData(Constants.KEY_NAME));
        binding.activityRegisterEtInputEmail.setText(new PreferenceManager(this).getData(Constants.KEY_EMAIL));
    }

    private void eventHandling() {
        binding.activityRegisterBtChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    private Bitmap getProfileImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}