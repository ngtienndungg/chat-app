package com.example.chat.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.chat.R;
import com.example.chat.activities.LoginActivity;
import com.example.chat.activities.ProfileActivity;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;

public class SelfFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_self, container, false);
        view.findViewById(R.id.llProfile).setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));
        view.findViewById(R.id.logOut).setOnClickListener(v -> signOut());
        RoundedImageView imageView = view.findViewById(R.id.ivProfile);
        imageView.setImageBitmap(getProfileImage(new PreferenceManager(requireContext()).getData(Constants.KEY_IMAGE)));
        return view;
    }

    private Bitmap getProfileImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void signOut() {
        PreferenceManager preferenceManager = new PreferenceManager(requireContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(user.getUid());
            HashMap<String, Object> updates = new HashMap<>();
            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
            documentReference.update(updates)
                    .addOnSuccessListener(unused -> {
                        FirebaseAuth.getInstance().signOut();
                        preferenceManager.clear();
                        startActivity(new Intent(requireActivity(), LoginActivity.class));
                        requireActivity().finish();
                    });
        }
    }
}