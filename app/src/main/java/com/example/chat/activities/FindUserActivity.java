package com.example.chat.activities;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityFindUserBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class FindUserActivity extends AppCompatActivity {
    private ActivityFindUserBinding binding;
    private Dialog userDialog;

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
            if (!Objects.requireNonNull(binding.fragmentFindUserEtEmail.getText()).toString().trim().isEmpty()) {
                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, binding.fragmentFindUserEtEmail.getText().toString().trim())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.getResult().getDocuments().get(0) != null) {
                                String name = task.getResult().getDocuments().get(0).getString(Constants.KEY_NAME);
                                String email = task.getResult().getDocuments().get(0).getString(Constants.KEY_EMAIL);
                                String image = task.getResult().getDocuments().get(0).getString(Constants.KEY_IMAGE);
                                userDialog = new Dialog(FindUserActivity.this);
                                userDialog.setContentView(R.layout.dialog_profile);
                                userDialog.setCanceledOnTouchOutside(true);
                                Objects.requireNonNull(userDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                                Objects.requireNonNull(userDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                userDialog.getWindow().getAttributes().windowAnimations = R.style.animation;
                                userDialog.findViewById(R.id.dialog_ivQuit).setOnClickListener(v1 -> {
                                    userDialog.dismiss();
                                    userDialog = null;
                                });
                                ImageView ivProfile = userDialog.findViewById(R.id.dialog_ivProfile);
                                ivProfile.setImageBitmap(getUserImage(image));
                                TextView tvName = userDialog.findViewById(R.id.dialog_tvName);
                                tvName.setText(name);
                                TextView tvEmail = userDialog.findViewById(R.id.dialog_tvEmail);
                                tvEmail.setText(email);
                                userDialog.show();
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