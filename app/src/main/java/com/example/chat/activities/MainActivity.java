package com.example.chat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.databinding.ActivityMainBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        displayUserProfile();
    }

    private void displayUserProfile() {
        PreferenceManager preferenceManager = new PreferenceManager(this);
        binding.activityMainTvName.setText(preferenceManager.getData(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getData(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.activityMainRivProfile.setImageBitmap(image);
    }
}