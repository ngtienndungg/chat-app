package com.example.chat.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;
import com.example.chat.adapters.ChatAdapter;
import com.example.chat.databinding.ActivityChatBinding;
import com.example.chat.models.Message;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User receivedUser;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    private List<Message> messages;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityChatIvSend.setColorFilter(getColor(R.color.primaryColor));
        initiate();
        eventHandling();
        listenMessages();
    }

    private void eventHandling() {
        binding.activityChatIvBack.setOnClickListener(v -> onBackPressed());
        binding.activityChatFlSend.setOnClickListener(v -> sendMessage());
        binding.activityChatEtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.activityChatEtMessage.getText().toString().trim().length() == 0) {
                    binding.activityChatIvSend.setColorFilter(getColor(R.color.secondaryText));
                    binding.activityChatFlSend.setEnabled(false);
                } else {
                    binding.activityChatIvSend.setColorFilter(getColor(R.color.colorWhite));
                    binding.activityChatFlSend.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private Bitmap getProfileImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void initiate() {
        binding.activityChatFlSend.setEnabled(false);
        receivedUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        currentUserId = FirebaseAuth.getInstance().getUid();
        binding.activityChatTvName.setText(Objects.requireNonNull(receivedUser).getName());
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                getProfileImage(receivedUser.getImage()),
                messages,
                currentUserId
        );
        binding.activityChatRvMessage.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, currentUserId);
        message.put(Constants.KEY_RECEIVER_ID, receivedUser.getId());
        message.put(Constants.KEY_MESSAGE, binding.activityChatEtMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        binding.activityChatEtMessage.setText(null);
    }

    private String formatDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm:a", Locale.getDefault()).format(date);
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message();
                    message.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    message.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    message.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE));
                    message.setDateTime(formatDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    message.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    messages.add(message);
                    Log.d("Message Check", message.getMessageContent() + " / " + message.getSenderId() + " / " + message.getReceiverId());
                }
            }
            messages.sort(Comparator.comparing(Message::getDateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(messages.size(), messages.size());
                binding.activityChatRvMessage.smoothScrollToPosition(messages.size() - 1);
            }
            binding.activityChatRvMessage.setVisibility(View.VISIBLE);
            Log.d("Message Check", String.valueOf(messages.size()));
        }
        binding.activityChatPbLoading.setVisibility(View.GONE);
    };
}