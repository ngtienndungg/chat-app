package com.example.chat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityFindUserBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;

import java.util.Objects;

public class FindUserActivity extends AppCompatActivity {
    private ActivityFindUserBinding binding;

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
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}