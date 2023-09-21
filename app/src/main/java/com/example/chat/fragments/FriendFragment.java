package com.example.chat.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.R;
import com.example.chat.activities.ChatActivity;
import com.example.chat.adapters.FriendAdapter;
import com.example.chat.adapters.RequestAdapter;
import com.example.chat.listeners.FriendListener;
import com.example.chat.listeners.RequestListener;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FriendFragment extends Fragment implements FriendListener, RequestListener {
    private int indexDocumentChange = 0;
    private ProgressBar pbLoading;
    private ProgressBar pbLoadingAccept;
    private ProgressBar pbLoadingDeny;
    private RecyclerView rvFriends;
    private RecyclerView rvRequest;
    private FriendAdapter friendAdapter;
    private RequestAdapter requestAdapter;
    private FirebaseFirestore database;
    private String currentUserId;
    private List<User> requestList;
    private List<User> friendList;
    private TextView tvRequests;
    private TextView tvFriends;
    private FirstCharComparator comparator;
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int finishIndexDocumentChange = value.getDocumentChanges().size() - 1;
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    if (Objects.equals(documentChange.getDocument().getString(Constants.KEY_STATUS), Constants.VALUE_STATUS_FRIEND)) {
                        fetchUserData(documentChange.getDocument().getString(Constants.KEY_USER_TO), documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP), user -> {
                            friendList.add(user);
                            if (indexDocumentChange++ == finishIndexDocumentChange) {
                                finishFetch();
                            }
                        });
                    } else if (Objects.equals(documentChange.getDocument().getString(Constants.KEY_STATUS), Constants.VALUE_STATUS_REQUEST_RECEIVED)) {
                        fetchUserData(documentChange.getDocument().getString(Constants.KEY_USER_TO), documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP), user -> {
                            requestList.add(user);
                            if (indexDocumentChange++ == finishIndexDocumentChange) {
                                finishFetch();
                            }
                        });
                    }
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    if (Objects.equals(documentChange.getDocument().getString(Constants.KEY_STATUS), Constants.VALUE_STATUS_FRIEND)) {
                        fetchUserData(documentChange.getDocument().getString(Constants.KEY_USER_TO), documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP), user -> {
                            friendList.add(user);
                            requestList.removeIf(request -> user.getId().equals(request.getId()));
                            if (indexDocumentChange++ == finishIndexDocumentChange) {
                                finishFetch();
                            }
                        });
                    }
                }
            }
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private void finishFetch() {
        indexDocumentChange = 0;
        friendList.sort(comparator);
        requestList.sort(Comparator.comparing(User::getDateObject));
        friendAdapter.notifyDataSetChanged();
        requestAdapter.notifyDataSetChanged();
        pbLoading.setVisibility(View.GONE);
        if (friendList.size() > 0) {
            rvFriends.setVisibility(View.VISIBLE);
            tvFriends.setVisibility(View.VISIBLE);
        } else {
            rvFriends.setVisibility(View.GONE);
            tvFriends.setVisibility(View.GONE);
        }
        if (requestList.size() > 0) {
            rvRequest.setVisibility(View.VISIBLE);
            tvRequests.setVisibility(View.VISIBLE);
        } else {
            rvRequest.setVisibility(View.GONE);
            tvRequests.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        viewMapping(view);
        getFriendAndRequestList();
        return view;
    }

    private void viewMapping(View view) {
        pbLoading = view.findViewById(R.id.fragment_friend_pbLoading);
        pbLoadingAccept = view.findViewById(R.id.item_container_user_pbLoadingAccept);
        pbLoadingDeny = view.findViewById(R.id.item_container_user_pbLoadingDeny);
        rvFriends = view.findViewById(R.id.fragment_friend_rvFriends);
        rvRequest = view.findViewById(R.id.fragment_friend_rvRequests);
        tvFriends = view.findViewById(R.id.fragment_friend_tvFriends);
        tvRequests = view.findViewById(R.id.fragment_friend_tvRequests);
        database = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        comparator = new FirstCharComparator();

        friendList = new ArrayList<>();
        requestList = new ArrayList<>();
        friendAdapter = new FriendAdapter(friendList, this);
        requestAdapter = new RequestAdapter(requestList, this);
        rvFriends.setAdapter(friendAdapter);
        rvRequest.setAdapter(requestAdapter);
    }

    private void getFriendAndRequestList() {
        pbLoading.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_COLLECTION_FRIENDS)
                .whereEqualTo(Constants.KEY_USER_FROM, currentUserId)
                .addSnapshotListener(eventListener);
    }

    private void fetchUserData(String userId, Date dateObject, DataCallback dataCallback) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        User user = new User();
                        user.setId(userId);
                        user.setName(task.getResult().getString(Constants.KEY_NAME));
                        user.setEmail(task.getResult().getString(Constants.KEY_EMAIL));
                        user.setImage(task.getResult().getString(Constants.KEY_IMAGE));
                        user.setDateObject(dateObject);
                        user.setDateTime(formatDateTime(dateObject));
                        dataCallback.onDataReceived(user);
                    }
                });
    }

    private String formatDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm:a", Locale.getDefault()).format(date);
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    @Override
    public void onAcceptClick(User user, int position) {
        database.collection(Constants.KEY_COLLECTION_FRIENDS)
                .whereEqualTo(Constants.KEY_USER_FROM, currentUserId)
                .whereEqualTo(Constants.KEY_USER_TO, user.getId())
                .get().addOnCompleteListener(task -> {
                    task.getResult()
                            .getDocuments()
                            .get(0)
                            .getReference()
                            .update(Constants.KEY_STATUS, Constants.VALUE_STATUS_FRIEND);
                });
        database.collection(Constants.KEY_COLLECTION_FRIENDS)
                .whereEqualTo(Constants.KEY_USER_FROM, user.getId())
                .whereEqualTo(Constants.KEY_USER_TO, currentUserId)
                .get().addOnCompleteListener(task -> {
                    task.getResult()
                            .getDocuments()
                            .get(0)
                            .getReference()
                            .update(Constants.KEY_STATUS, Constants.VALUE_STATUS_FRIEND);
                });
    }

    @Override
    public void onDenyClick(User user) {

    }

    private interface DataCallback {
        void onDataReceived(User user);
    }

    private static class FirstCharComparator implements Comparator<User> {
        @Override
        public int compare(User userOne, User userTwo) {
            return Character.compare(userOne.getName().charAt(0), userTwo.getName().charAt(0));
        }
    }
}