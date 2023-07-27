package com.example.chat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityMainBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        eventHandling();
        displayUserProfile();
        getToken();
    }

    private void eventHandling() {
        binding.activityMainIvSignOut.setOnClickListener(v -> signOut());
        binding.activityMainBtAddMessage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UserActivity.class));
        });
    }

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            binding.activityMainIvSignOut.setVisibility(View.INVISIBLE);
            binding.activityMainPbSignOut.setVisibility(View.VISIBLE);
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(user.getUid());
            HashMap<String, Object> updates = new HashMap<>();
            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
            documentReference.update(updates)
                    .addOnSuccessListener(unused -> {
                        FirebaseAuth.getInstance().signOut();
                        preferenceManager.clear();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> displayToast(getString(R.string.toast_something_wrong)));
        }
    }

    private void displayUserProfile() {
        preferenceManager = new PreferenceManager(this);
        binding.activityMainTvName.setText(preferenceManager.getData(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getData(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.activityMainRivProfile.setImageBitmap(image);
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        if (user != null) {
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(user.getUid());
            documentReference.update(Constants.KEY_FCM_TOKEN, token);
        }
    }
}