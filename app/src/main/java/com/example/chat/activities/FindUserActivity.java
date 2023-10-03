package com.example.chat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityFindUserBinding;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FindUserActivity extends AppCompatActivity {
    private User searchedUser;
    private ActivityFindUserBinding binding;
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            if (value.getDocuments().size() == 0) {
                // Stranger
                actionDisplay(true, false, false, false);
                binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_stranger));
            } else {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED || documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        String status = documentChange.getDocument().getString(Constants.KEY_STATUS);
                        // Friend
                        if (Objects.equals(status, Constants.VALUE_STATUS_FRIEND)) {
                            actionDisplay(false, false, true, false);
                        }
                        // Receive Request
                        else if (Objects.equals(status, Constants.VALUE_STATUS_REQUEST_RECEIVED)) {
                            actionDisplay(false, true, false, false);
                        }
                        // Sent Request
                        else if (Objects.equals(status, Constants.VALUE_STATUS_REQUEST_SENT)) {
                            actionDisplay(false, false, false, true);
                        }
                    }
                }
            }
        }
    };
    private String currentUserId;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initiate();
        eventHandling();
    }

    private void initiate() {
        PreferenceManager preferenceManager = new PreferenceManager(this);
        binding.fragmentFindUserTvName.setText(preferenceManager.getData(Constants.KEY_NAME));
        binding.fragmentFindUserIvImage.setImageBitmap(getUserImage(preferenceManager.getData(Constants.KEY_IMAGE)));
        currentUserId = FirebaseAuth.getInstance().getUid();
        database = FirebaseFirestore.getInstance();
        searchedUser = new User();
    }

    private void eventHandling() {
        binding.fragmentFindUserIvBack.setOnClickListener(v -> finish());
        binding.fragmentFindUserFlNext.setOnClickListener(v -> {
            if (Objects.requireNonNull(binding.fragmentFindUserEtEmail.getText()).toString().isEmpty()
                    || !Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(binding.fragmentFindUserEtEmail.getText()).toString()).matches()) {
                binding.fragmentFindUserTilEmail.setHelperTextEnabled(true);
                binding.fragmentFindUserTilEmail.setHelperText(getString(R.string.please_type_a_valid_email));
            } else {
                binding.fragmentFindUserTilEmail.setHelperTextEnabled(false);
                binding.fragmentFindUserPbLoading.setVisibility(View.VISIBLE);
                binding.fragmentFindUserCtlSearchedUser.setVisibility(View.GONE);
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, binding.fragmentFindUserEtEmail.getText().toString().trim())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.getResult().getDocuments().size() != 0) {
                                if (task.getResult().getDocuments().get(0) != null) {
                                    searchedUser.setId(task.getResult().getDocuments().get(0).getId());
                                    searchedUser.setImage(task.getResult().getDocuments().get(0).getString(Constants.KEY_IMAGE));
                                    searchedUser.setName(task.getResult().getDocuments().get(0).getString(Constants.KEY_NAME));
                                    binding.fragmentFindUserIvSearchedProfileUser.setImageBitmap(getUserImage(searchedUser.getImage()));
                                    binding.fragmentFindUserTvSearchedNameUser.setText(searchedUser.getName());
                                    // Yourself
                                    if (task.getResult().getDocuments().get(0).getId().equals(currentUserId)) {
                                        actionDisplay(false, false, false, false);
                                    }
                                    // Other
                                    else {
                                        database.collection(Constants.KEY_COLLECTION_FRIENDS)
                                                .whereEqualTo(Constants.KEY_USER_FROM, currentUserId)
                                                .whereEqualTo(Constants.KEY_USER_TO, searchedUser.getId())
                                                .addSnapshotListener(eventListener);
                                    }
                                }
                            } else {
                                binding.fragmentFindUserTilEmail.setHelperTextEnabled(true);
                                binding.fragmentFindUserPbLoading.setVisibility(View.GONE);
                                binding.fragmentFindUserTilEmail.setHelperText(getResources().getString(R.string.no_user_match));
                            }
                        });
            }
        });
        actionEventHandling();
    }

    private void actionEventHandling() {
        binding.fragmentFindUserCtlAddFriendAction.setOnClickListener(v -> {
            Date currentTime = new Date();
            Map<String, Object> requestSentData = new HashMap<>();
            requestSentData.put(Constants.KEY_USER_FROM, currentUserId);
            requestSentData.put(Constants.KEY_USER_TO, searchedUser.getId());
            requestSentData.put(Constants.KEY_STATUS, Constants.VALUE_STATUS_REQUEST_SENT);
            requestSentData.put(Constants.KEY_TIMESTAMP, currentTime);
            Map<String, Object> requestReceivedData = new HashMap<>();
            requestReceivedData.put(Constants.KEY_USER_FROM, searchedUser.getId());
            requestReceivedData.put(Constants.KEY_USER_TO, currentUserId);
            requestReceivedData.put(Constants.KEY_STATUS, Constants.VALUE_STATUS_REQUEST_RECEIVED);
            requestReceivedData.put(Constants.KEY_TIMESTAMP, currentTime);
            database.collection(Constants.KEY_COLLECTION_FRIENDS).document().set(requestSentData);
            database.collection(Constants.KEY_COLLECTION_FRIENDS).document().set(requestReceivedData);
        });
        binding.fragmentFindUserCtlUnsentAction.setOnClickListener(v -> {
            getUpdateTask(currentUserId, searchedUser.getId()).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().delete());
            getUpdateTask(searchedUser.getId(), currentUserId).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().delete());
        });
        binding.fragmentFindUserCtlRejectAction.setOnClickListener(v -> {
            getUpdateTask(currentUserId, searchedUser.getId()).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().delete());
            getUpdateTask(searchedUser.getId(), currentUserId).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().delete());
        });
        binding.fragmentFindUserCtlAcceptAction.setOnClickListener(v -> {
            getUpdateTask(currentUserId, searchedUser.getId()).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().update(Constants.KEY_STATUS, Constants.VALUE_STATUS_FRIEND));
            getUpdateTask(searchedUser.getId(), currentUserId).addOnCompleteListener(task -> task.getResult().getDocuments().get(0).getReference().update(Constants.KEY_STATUS, Constants.VALUE_STATUS_FRIEND));
        });
        binding.fragmentFindUserCtlMessageAction.setOnClickListener(v -> {
            Intent intent = new Intent(FindUserActivity.this, ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, searchedUser);
            startActivity(intent);
        });
    }

    private Task<QuerySnapshot> getUpdateTask(String userFrom, String userTo) {
        return database.collection(Constants.KEY_COLLECTION_FRIENDS)
                .whereEqualTo(Constants.KEY_USER_FROM, userFrom)
                .whereEqualTo(Constants.KEY_USER_TO, userTo)
                .get();
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void actionDisplay(boolean strangerView, boolean requestView, boolean friendView, boolean sentView) {
        if (strangerView) {
            binding.fragmentFindUserCtlRejectAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlMessageAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAcceptAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlUnsentAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAddFriendAction.setVisibility(View.VISIBLE);
            binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_stranger));
        } else if (requestView) {
            binding.fragmentFindUserCtlMessageAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAddFriendAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlUnsentAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlRejectAction.setVisibility(View.VISIBLE);
            binding.fragmentFindUserCtlAcceptAction.setVisibility(View.VISIBLE);
            binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_stranger));
        } else if (friendView) {
            binding.fragmentFindUserCtlRejectAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAddFriendAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAcceptAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlUnsentAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlMessageAction.setVisibility(View.VISIBLE);
            binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_friend));
        } else if (sentView) {
            binding.fragmentFindUserCtlRejectAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAddFriendAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAcceptAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlMessageAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlUnsentAction.setVisibility(View.VISIBLE);
            binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_stranger));
        } else {
            binding.fragmentFindUserCtlRejectAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAddFriendAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlAcceptAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlMessageAction.setVisibility(View.GONE);
            binding.fragmentFindUserCtlUnsentAction.setVisibility(View.GONE);
            binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_yourself));
        }
        binding.fragmentFindUserPbLoading.setVisibility(View.GONE);
        binding.fragmentFindUserCtlSearchedUser.setVisibility(View.VISIBLE);
    }
}