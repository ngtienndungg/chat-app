package com.example.chat.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.adapters.UserAdapter;
import com.example.chat.databinding.ActivityUserBinding;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserActivity extends AppCompatActivity {
    private ActivityUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getUserList();
        eventHandling();
    }

    private void eventHandling() {
        binding.activityUserIvBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUserList() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    List<User> users = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        String currentUser = FirebaseAuth.getInstance().getUid();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (Objects.requireNonNull(currentUser).equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                            user.setEmail(queryDocumentSnapshot.getString(Constants.KEY_EMAIL));
                            user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                            user.setFcmToken(queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN));
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(users);
                            binding.activityUserRvUser.setAdapter(userAdapter);
                            binding.activityUserRvUser.setVisibility(View.VISIBLE);
                        } else {
                            displayErrorMessage();
                        }
                    } else {
                        displayErrorMessage();
                    }
                });
    }

    private void displayErrorMessage() {
        binding.activityUserTvErrorMessage.setText(getString(R.string.no_user));
        binding.activityUserTvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.activityUserPbLoading.setVisibility(View.VISIBLE);
        } else {
            binding.activityUserPbLoading.setVisibility(View.GONE);
        }
    }
}