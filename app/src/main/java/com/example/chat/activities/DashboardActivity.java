package com.example.chat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.chat.R;
import com.example.chat.databinding.ActivityDashboardBinding;
import com.example.chat.fragments.FriendFragment;
import com.example.chat.fragments.SelfFragment;
import com.example.chat.fragments.RecentConversationFragment;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class DashboardActivity extends BaseActivity {

    private Fragment mainFragment;
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

    @SuppressLint("NonConstantResourceId")
    private void eventHandling() {
        binding.activityDashboardBnvNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_item_message:
                    if (!(mainFragment instanceof RecentConversationFragment)) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .remove(mainFragment)
                                .commit();
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
                                .remove(mainFragment)
                                .commit();
                        mainFragment = new FriendFragment();
                        loadFragment(mainFragment);
                        return true;
                    } else {
                        return false;
                    }
                case R.id.menu_item_profile:
                    if (!(mainFragment instanceof SelfFragment)) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .remove(mainFragment)
                                .commit();
                        mainFragment = new SelfFragment();
                        loadFragment(mainFragment);
                        return true;
                    } else {
                        return false;
                    }
            }
            return false;
        });

        binding.activityDashboardRlAdd.setOnClickListener(v -> startActivity(new Intent(this, FindUserActivity.class)));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_dashboard_fragment_container, fragment)
                .commit();
    }

    /* private void signOut() {
        database = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
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
    } */

    private void displayUserProfile() {
        preferenceManager = new PreferenceManager(this);
        byte[] bytes = Base64.decode(preferenceManager.getData(Constants.KEY_IMAGE), Base64.DEFAULT);
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
            binding.activityDashboardBnvNavigation.setSelectedItemId(R.id.menu_item_message);
            mainFragment = new RecentConversationFragment();
            loadFragment(mainFragment);
        } else {
            super.onBackPressed();
        }
    }
}