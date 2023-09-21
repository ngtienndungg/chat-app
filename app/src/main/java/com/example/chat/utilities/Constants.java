package com.example.chat.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_COLLECTION_FRIENDS = "friends";
    public static final String KEY_USER_FROM = "userFrom";
    public static final String KEY_USER_TO = "userTo";
    public static final String KEY_STATUS = "status";
    public static final String VALUE_STATUS_FRIEND = "friend";
    public static final String VALUE_STATUS_REQUEST_SENT = "requestSent";
    public static final String VALUE_STATUS_REQUEST_RECEIVED = "requestReceived";
    public static final String VALUE_STATUS_REQUEST_REJECTED = "requestRejected";
    public static final String VALUE_STATUS_FRIEND_DELETED = "friendDeleted";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public final static String KEY_USER_AVAILABILITY = "userAvailability";
    public final static String REMOTE_MSG_DATA = "data";
    public final static String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public final static String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public final static String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAPFGj2os:APA91bF0iAweJIus1_Z0m9LLBC84SvZRKaiRHKedE2P7ycBtCdPDLs6xgFlN5vg4qJFo3g3J0PeRdBQi2hugiLnN6lGqLxz2uU9V9YzkOjgISFl1YEMxyJudvdfEpPZIeB_aPOlWufZS"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
