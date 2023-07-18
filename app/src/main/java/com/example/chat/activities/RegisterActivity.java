package com.example.chat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private String encodedImage;
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri uri = result.getData().getData();
                        try {
                            assert uri != null;
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.activityRegisterIvProfile.setImageBitmap(bitmap);
                            binding.activityRegisterTvSelectImage.setText(getString(R.string.change_image));
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            Toast.makeText(this, getString(R.string.toast_something_wrong), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventHandling();
    }

    private void eventHandling() {
        binding.activityRegisterBtLogin.setOnClickListener(v -> onBackPressed());
        binding.activityRegisterBtSignUp.setOnClickListener(v -> signUp());
        binding.activityRegisterTvSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void displayError(EditText viewDisplay, String message) {
        viewDisplay.setError(message);
        viewDisplay.requestFocus();
    }

    private void signUp() {
        if (isValidInformation()) {
            loading(true);
            String email = binding.activityRegisterEtInputEmail.getText().toString().trim();
            String password = binding.activityRegisterEtInputPassword.getText().toString();
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, createTask -> {
                        if (createTask.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            Objects.requireNonNull(user).sendEmailVerification().addOnCompleteListener(sendTask -> {
                                loading(false);
                                startActivity(new Intent(RegisterActivity.this, NotificationActivity.class));
                            });
                        } else {
                            loading(false);
                            String errorCode = ((FirebaseAuthException) Objects.requireNonNull(createTask.getException())).getErrorCode();
                            switch (errorCode) {
                                case "ERROR_EMAIL_ALREADY_IN_USE":
                                    displayError(binding.activityRegisterEtInputEmail, getString(R.string.set_error_email_already_use));
                                    break;
                                case "ERROR_WEAK_PASSWORD":
                                    displayError(binding.activityRegisterEtInputPassword, getString(R.string.set_error_weak_password));
                                    break;
                            }
                        }
                    });
        }
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Boolean isValidInformation() {
        if (encodedImage == null) {
            Toast.makeText(this, getString(R.string.toast_empty_profile_image), Toast.LENGTH_SHORT).show();
            return false;
        } else if (binding.activityRegisterEtInputName.getText().toString().trim().isEmpty()) {
            displayError(binding.activityRegisterEtInputName, getString(R.string.set_error_empty_name));
            return false;
        } else if (binding.activityRegisterEtInputEmail.getText().toString().trim().isEmpty()) {
            displayError(binding.activityRegisterEtInputEmail, getString(R.string.set_error_empty_email));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.activityRegisterEtInputEmail.getText().toString().trim()).matches()) {
            displayError(binding.activityRegisterEtInputEmail, getString(R.string.set_error_invalid_email));
            return false;
        } else if (binding.activityRegisterEtInputPassword.getText().toString().trim().isEmpty()) {
            displayError(binding.activityRegisterEtInputPassword, getString(R.string.set_error_empty_password));
            return false;
        } else if (binding.activityRegisterEtInputConfirmPassword.getText().toString().trim().isEmpty()) {
            displayError(binding.activityRegisterEtInputConfirmPassword, getString(R.string.set_error_empty_confirm_password));
            return false;
        } else if (!binding.activityRegisterEtInputPassword.getText().toString().equals(binding.activityRegisterEtInputConfirmPassword.getText().toString())) {
            displayError(binding.activityRegisterEtInputConfirmPassword, getString(R.string.set_error_not_match_password));
            return false;
        }
        return true;
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.activityRegisterBtSignUp.setVisibility(View.INVISIBLE);
            binding.activityRegisterPbSignUp.setVisibility(View.VISIBLE);
        } else {
            binding.activityRegisterBtSignUp.setVisibility(View.VISIBLE);
            binding.activityRegisterPbSignUp.setVisibility(View.INVISIBLE);
        }
    }

}