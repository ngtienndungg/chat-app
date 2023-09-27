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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class FindUserActivity extends AppCompatActivity {
    private ActivityFindUserBinding binding;
    private String currentUserId;

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
            }
        });
        binding.fragmentFindUserIvNext.setOnClickListener(v -> {
            binding.fragmentFindUserCtlSearchedUser.setVisibility(View.GONE);
            if (!Objects.requireNonNull(binding.fragmentFindUserEtEmail.getText()).toString().trim().isEmpty()) {
                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, binding.fragmentFindUserEtEmail.getText().toString().trim())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.getResult().getDocuments().get(0) != null) {
                                String userToUid = task.getResult().getDocuments().get(0).getId();
                                binding.fragmentFindUserIvSearchedProfileUser.setImageBitmap(getUserImage(task.getResult().getDocuments().get(0).getString(Constants.KEY_IMAGE)));
                                binding.fragmentFindUserTvSearchedNameUser.setText(task.getResult().getDocuments().get(0).getString(Constants.KEY_NAME));
                                if (task.getResult().getDocuments().get(0).getId().equals(currentUserId)) {
                                    binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_yourself));
                                    binding.fragmentFindUserCtlSearchedUser.setVisibility(View.VISIBLE);
                                } else {
                                    FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_FRIENDS)
                                            .whereEqualTo(Constants.KEY_USER_FROM, currentUserId)
                                            .whereEqualTo(Constants.KEY_USER_TO, userToUid)
                                            .get()
                                            .addOnCompleteListener(relationshipTask -> {
                                                if (relationshipTask.getResult().getDocuments().size() == 0) {
                                                    binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_stranger));
                                                } else {
                                                    String status = relationshipTask.getResult().getDocuments().get(0).getString(Constants.KEY_STATUS);
                                                    if (Objects.equals(status, Constants.VALUE_STATUS_FRIEND)) {
                                                        binding.fragmentFindUserTvRelationship.setText(getResources().getString(R.string.relationship_friend));
                                                    }
                                                }
                                                binding.fragmentFindUserCtlSearchedUser.setVisibility(View.VISIBLE);
                                            });
                                }
                            }
                        });
            }
        });
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}