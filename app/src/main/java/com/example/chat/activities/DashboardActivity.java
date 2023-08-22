package com.example.chat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.chat.R;
import com.example.chat.databinding.ActivityDashboardBinding;
import com.example.chat.fragments.FriendFragment;
import com.example.chat.fragments.ProfileFragment;
import com.example.chat.fragments.RecentConversationFragment;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class DashboardActivity extends BaseActivity {

    Fragment mainFragment;
    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener onItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.menu_item_message:
                if (!(mainFragment instanceof RecentConversationFragment)) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .remove(mainFragment);
                    mainFragment = new RecentConversationFragment();
                    loadFragment(mainFragment);
                    return true;
                } else {
                    return false;
                }
            case R.id.menu_item_friends:
                if (!(mainFragment instanceof FriendFragment)) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .remove(mainFragment);
                    mainFragment = new FriendFragment();
                    loadFragment(mainFragment);
                    return true;
                } else {
                    return false;
                }
            case R.id.menu_item_profile:
                if (!(mainFragment instanceof ProfileFragment)) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .remove(mainFragment);
                    mainFragment = new ProfileFragment();
                    loadFragment(mainFragment);
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    };
    private ActivityDashboardBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initiate();
        eventHandling();
        displayUserProfile();
        getToken();
    }

    private void initiate() {
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        mainFragment = new RecentConversationFragment();
        loadFragment(mainFragment);
    }

    private void eventHandling() {
        binding.activityMainIvSignOut.setOnClickListener(v -> signOut());
        binding.activityMainBnvNavigation.setOnItemSelectedListener(onItemSelectedListener);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main_fragment_container, fragment)
                .commit();
    }

    private void signOut() {
        database = FirebaseFirestore.getInstance();
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
        preferenceManager.putData(Constants.KEY_FCM_TOKEN, token);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(user.getUid());
            documentReference.update(Constants.KEY_FCM_TOKEN, token);
        }
    }

    @Override
    public void onBackPressed() {
        if (!(mainFragment instanceof RecentConversationFragment)) {
            binding.activityMainBnvNavigation.setSelectedItemId(R.id.menu_item_message);
            mainFragment = new RecentConversationFragment();
            loadFragment(mainFragment);
        } else {
            super.onBackPressed();
        }
    }
}