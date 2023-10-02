package com.example.chat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityFindUserBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class FindUserActivity extends AppCompatActivity {
    private ActivityFindUserBinding binding;
    private String currentUserId;
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
                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, binding.fragmentFindUserEtEmail.getText().toString().trim())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.getResult().getDocuments().size() != 0) {
                                if (task.getResult().getDocuments().get(0) != null) {
                                    String userToUid = task.getResult().getDocuments().get(0).getId();
                                    binding.fragmentFindUserIvSearchedProfileUser.setImageBitmap(getUserImage(task.getResult().getDocuments().get(0).getString(Constants.KEY_IMAGE)));
                                    binding.fragmentFindUserTvSearchedNameUser.setText(task.getResult().getDocuments().get(0).getString(Constants.KEY_NAME));
                                    // Yourself
                                    if (task.getResult().getDocuments().get(0).getId().equals(currentUserId)) {
                                        actionDisplay(false, false, false, false);
                                    }
                                    // Other
                                    else {
                                        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_FRIENDS)
                                                .whereEqualTo(Constants.KEY_USER_FROM, currentUserId)
                                                .whereEqualTo(Constants.KEY_USER_TO, userToUid)
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