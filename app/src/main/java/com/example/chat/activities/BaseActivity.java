package com.example.chat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_USER_AVAILABILITY, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_USER_AVAILABILITY, false);
    }
}
