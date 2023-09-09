package com.example.chat.fragments;

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
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendFragment extends Fragment implements FriendListener {
    private ProgressBar pbLoading;
    private RecyclerView rvFriends;
    private RecyclerView rvRequest;
    private FirebaseFirestore database;
    private String currentUserId;
    private List<User> requestList;
    private List<User> friendList;
    private TextView tvRequests;
    private TextView tvFriends;
    private int finishCountRequest = 0;
    private int finishCountFriend = 0;


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

    private interface RequestAndFriendListCallback {
        void onCallback(List<User> friendList, List<User> requestList);
    }

    private void viewMapping(View view) {
        pbLoading = view.findViewById(R.id.fragment_friend_pbLoading);
        rvFriends = view.findViewById(R.id.fragment_friend_rvFriends);
        rvRequest = view.findViewById(R.id.fragment_friend_rvRequests);
        tvFriends = view.findViewById(R.id.fragment_friend_tvFriends);
        tvRequests = view.findViewById(R.id.fragment_friend_tvRequests);
        database = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            pbLoading.setVisibility(View.VISIBLE);
        } else {
            pbLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    private void getFriendAndRequestList() {
        getList((friendList, requestList) -> {
            if (requestList.size() > 0) {
                for (int i = 0; i < requestList.size(); i++) {
                    fillUpRequestUserData(requestList.get(i), requestList);
                }
            }
            if (friendList.size() > 0) {
                for (int i = 0; i < friendList.size(); i++) {
                    fillUpFriendUserData(friendList.get(i), friendList);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void getList(RequestAndFriendListCallback requestAndFriendListCallback) {
        loading(true);
        requestList = new ArrayList<>();
        friendList = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_USERS).document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> friendData;
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            friendData = (HashMap<String, Object>) documentSnapshot.get(Constants.KEY_FRIENDS);
                            if (friendData != null) {
                                for (Map.Entry<String, Object> friend : friendData.entrySet()) {
                                    User user = new User();
                                    user.setId(friend.getKey());
                                    if (friend.getValue().equals(0L)) {
                                        friendList.add(user);
                                    } else {
                                        requestList.add(user);
                                    }
                                }
                            }
                        }
                        requestAndFriendListCallback.onCallback(friendList, requestList);
                    }
                });
    }


    private void fillUpRequestUserData(User user, List<User> list) {
        database.collection(Constants.KEY_COLLECTION_USERS).document(user.getId()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        user.setId(document.getString(Constants.KEY_USER_ID));
                        user.setName(document.getString(Constants.KEY_NAME));
                        user.setImage(document.getString(Constants.KEY_IMAGE));
                        user.setEmail(document.getString(Constants.KEY_EMAIL));
                    }
                    if (finishCountRequest++ == list.size() - 1) {
                        loading(false);
                        RequestAdapter requestAdapter = new RequestAdapter(requestList);
                        rvRequest.setAdapter(requestAdapter);
                        rvRequest.setVisibility(View.VISIBLE);
                        tvRequests.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void fillUpFriendUserData(User user, List<User> list) {
        database.collection(Constants.KEY_COLLECTION_USERS).document(user.getId()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        user.setId(document.getString(Constants.KEY_USER_ID));
                        user.setName(document.getString(Constants.KEY_NAME));
                        user.setImage(document.getString(Constants.KEY_IMAGE));
                        user.setEmail(document.getString(Constants.KEY_EMAIL));
                    }
                    if (finishCountFriend++ == list.size() - 1) {
                        loading(false);
                        FriendAdapter friendAdapter = new FriendAdapter(list, this);
                        rvFriends.setAdapter(friendAdapter);
                        rvFriends.setVisibility(View.VISIBLE);
                        tvFriends.setVisibility(View.VISIBLE);
                    }
                });
    }
}