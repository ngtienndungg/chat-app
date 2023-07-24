package com.example.chat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.databinding.ActivityMainBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        displayUserProfile();

    }

    private void displayUserProfile() {
        preferenceManager = new PreferenceManager(this);
        binding.activityMainTvName.setText(preferenceManager.getData(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getData(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.activityMainRivProfile.setImageBitmap(image);
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

    }
}