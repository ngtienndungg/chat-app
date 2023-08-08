package com.example.chat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.adapters.RecentConversationAdapter;
import com.example.chat.databinding.ActivityMainBinding;
import com.example.chat.models.Message;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<Message> conversations;
    private RecentConversationAdapter recentConversationAdapter;
    private FirebaseFirestore database;
    private String currentUserUid;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initiate();
        eventHandling();
        displayUserProfile();
        getToken();
        listenConversation();
    }

    private void initiate() {
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getUid();
        conversations = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(conversations);
        binding.activityMainRvRecentMessage.setAdapter(recentConversationAdapter);
    }

    private void eventHandling() {
        binding.activityMainIvSignOut.setOnClickListener(v -> signOut());
        binding.activityMainBtAddMessage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UserActivity.class));
        });
    }

    private void signOut() {
        database = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            binding.activityMainIvSignOut.setVisibility(View.INVISIBLE);
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
    }

    private void displayUserProfile() {
        preferenceManager = new PreferenceManager(this);
        binding.activityMainTvName.setText(preferenceManager.getData(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getData(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.activityMainRivProfile.setImageBitmap(image);
    }

    public void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateToken(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(user.getUid());
            documentReference.update(Constants.KEY_FCM_TOKEN, token);
        }
    }

    private void listenConversation() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserUid)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserUid)
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    Message message = new Message();
                    message.setSenderId(senderId);
                    message.setReceiverId(receiverId);
                    if (currentUserUid.equals(senderId)) {
                        message.setConversationId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                        message.setConversationName(documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME));
                        message.setConversationImage(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE));
                    } else {
                        message.setConversationId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                        message.setConversationName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                        message.setConversationImage(documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE));
                    }
                    message.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    message.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    conversations.add(message);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)) {
                            conversations.get(i).setMessageContent(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                            conversations.get(i).setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            conversations.sort(Comparator.comparing(Message::getDateObject));
            recentConversationAdapter.notifyDataSetChanged();
            binding.activityMainRvRecentMessage.smoothScrollToPosition(0);
            binding.activityMainRvRecentMessage.setVisibility(View.VISIBLE);
            binding.activityChatPbLoading.setVisibility(View.GONE);
            Toast.makeText(this, conversations.size() + " ", Toast.LENGTH_SHORT).show();
        }
    };
}