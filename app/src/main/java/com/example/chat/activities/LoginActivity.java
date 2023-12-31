package com.example.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.databinding.ActivityLoginBinding;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventHandling();
    }

    private void eventHandling() {
        binding.activityLoginTvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));
        binding.activityLoginBtSignIn.setOnClickListener(v -> signIn());
        binding.activityLoginTvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void signIn() {
        if (binding.activityLoginEtInputEmail.getText().toString().trim().isEmpty()) {
            setError(binding.activityLoginEtInputEmail, getString(R.string.set_error_empty_email));
        } else if (binding.activityLoginEtInputPassword.getText().toString().trim().isEmpty()) {
            setError(binding.activityLoginEtInputPassword, getString(R.string.set_error_empty_password));
        } else {
            loading(true);
            String email = binding.activityLoginEtInputEmail.getText().toString().trim();
            String password = binding.activityLoginEtInputPassword.getText().toString();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (Objects.requireNonNull(currentUser).isEmailVerified()) {
                                FirebaseFirestore database = FirebaseFirestore.getInstance();
                                database.collection(Constants.KEY_COLLECTION_USERS)
                                        .whereEqualTo(Constants.KEY_EMAIL, email)
                                        .get()
                                        .addOnCompleteListener(documentTask -> {
                                            if (documentTask.getResult() != null && documentTask.getResult().getDocuments().size() > 0) {
                                                DocumentSnapshot documentSnapshot = documentTask.getResult().getDocuments().get(0);
                                                PreferenceManager preferenceManager = new PreferenceManager(this);
                                                preferenceManager.putData(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                                preferenceManager.putData(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                                preferenceManager.putData(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                                                loading(false);
                                                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            } else {
                                loading(false);
                                setNotification(getString(R.string.not_verify));
                                FirebaseAuth.getInstance().signOut();
                            }
                        } else {
                            loading(false);
                            setNotification(getString(R.string.login_failure));
                            binding.activityLoginEtInputEmail.requestFocus();
                        }
                    });
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.activityLoginBtSignIn.setVisibility(View.INVISIBLE);
            binding.activityLoginPbSignIn.setVisibility(View.VISIBLE);
        } else {
            binding.activityLoginBtSignIn.setVisibility(View.VISIBLE);
            binding.activityLoginPbSignIn.setVisibility(View.INVISIBLE);
        }
    }

    private void setError(EditText viewDisplay, String message) {
        viewDisplay.setError(message);
        viewDisplay.requestFocus();
    }

    private void setNotification(String message) {
        binding.activityLoginTvNotification.setText(message);
        binding.activityLoginTvNotification.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }
}