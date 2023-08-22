package com.example.chat.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.R;
import com.example.chat.activities.ChatActivity;
import com.example.chat.adapters.RecentConversationAdapter;
import com.example.chat.listeners.ChatListener;
import com.example.chat.models.Message;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecentConversationFragment extends Fragment implements ChatListener {
    private FirebaseFirestore database;
    private List<Message> conversations;
    private RecentConversationAdapter recentConversationAdapter;
    private String currentUserUid;
    ProgressBar pbLoading;
    RecyclerView rvRecentMessage;

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
            rvRecentMessage.smoothScrollToPosition(0);
            rvRecentMessage.setVisibility(View.VISIBLE);
            pbLoading.setVisibility(View.GONE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_conversation, container, false);
        pbLoading = (ProgressBar) view.findViewById(R.id.fragment_recent_conversation_pbLoading);
        rvRecentMessage = (RecyclerView) view.findViewById(R.id.fragment_recent_conversation_rvRecentMessage);
        initiate();
        listenConversation();
        return view;
    }

    private void initiate() {
        database = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getUid();
        conversations = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(conversations, this);
        rvRecentMessage.setAdapter(recentConversationAdapter);
    }

    private void listenConversation() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserUid)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserUid)
                .addSnapshotListener(eventListener);
    }

    @Override
    public void onRecentConversationClicked(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}