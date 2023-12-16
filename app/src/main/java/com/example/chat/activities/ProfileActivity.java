package com.example.chat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityProfileBinding;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
        if (getIntent().getStringExtra("FromChat") != null) {
            User user = (User) getIntent().getSerializableExtra("User");
            binding.activityProfileBtChangePassword.setVisibility(View.GONE);
            binding.activityProfileBtUpdate.setText("Delete Friend");
            binding.activityProfileBtUpdate.setBackgroundColor(getColor(R.color.colorError));
            binding.activityProfileTvSelectImage.setVisibility(View.GONE);
            binding.activityProfileEtInputEmail.setFocusable(false);
            binding.activityProfileEtInputName.setFocusable(false);
            binding.activityProfileIvProfile.setImageBitmap(getProfileImage(user.getImage()));
            binding.activityProfileEtInputName.setText(user.getName());
            binding.activityProfileEtInputEmail.setText(user.getEmail());
            binding.activityProfileBtUpdate.setOnClickListener(v -> {
                getUpdateTask(new PreferenceManager(this).getData(Constants.KEY_USER_ID), user.getId()).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().delete());
                getUpdateTask(user.getId(), new PreferenceManager(this).getData(Constants.KEY_USER_ID)).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().delete());
                onBackPressed();
            });
        } else {
            binding.activityProfileIvProfile.setImageBitmap(getProfileImage(new PreferenceManager(this).getData(Constants.KEY_IMAGE)));
            binding.activityProfileEtInputName.setText(new PreferenceManager(this).getData(Constants.KEY_NAME));
            binding.activityProfileEtInputEmail.setText(new PreferenceManager(this).getData(Constants.KEY_EMAIL));
        }
    }

    private Task<QuerySnapshot> getUpdateTask(String userFrom, String userTo) {
        return FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_FRIENDS)
                .whereEqualTo(Constants.KEY_USER_FROM, userFrom)
                .whereEqualTo(Constants.KEY_USER_TO, userTo)
                .get();
    }


    private void eventHandling() {
        binding.activityProfileBtChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    private Bitmap getProfileImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}