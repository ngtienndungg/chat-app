package com.example.chat.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.chat.R;
import com.example.chat.adapters.ChatAdapter;
import com.example.chat.databinding.ActivityChatBinding;
import com.example.chat.listeners.MessageListener;
import com.example.chat.models.Message;
import com.example.chat.models.User;
import com.example.chat.network.ApiClient;
import com.example.chat.network.ApiService;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements MessageListener {
    private ActivityChatBinding binding;
    private User receivedUser;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private List<Message> messages;
    private String currentUserId;
    private String conversationId;
    private boolean isOnline;
    ActivityResultLauncher<String> getImage = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Long time = System.currentTimeMillis();
                    String imagePath = "images/" + currentUserId + "/" + time;
                    Bitmap bitmap;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                    byte[] fileInBytes = byteArrayOutputStream.toByteArray();
                    FirebaseStorage.getInstance().getReference(imagePath).putBytes(fileInBytes).addOnCompleteListener(task -> {
                        sendMessage(imagePath);
                    });
                }
            });
    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };
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
                    if (documentChange.getDocument().getString(Constants.KEY_MESSAGE) != null) {
                        message.setMessage(documentChange.getDocument().getString(Constants.KEY_MESSAGE));
                    } else {
                        message.setMessageImage(documentChange.getDocument().getString(Constants.KEY_IMAGE_MESSAGE));
                    }
                    message.setDateTime(formatDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    message.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    messages.add(message);
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
        }
        binding.activityChatPbLoading.setVisibility(View.GONE);
        if (conversationId == null) {
            checkConversation();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityChatIvSend.setColorFilter(getColor(R.color.secondaryText));
        initiate();
        eventHandling();
        listenMessages();
    }

    private void eventHandling() {
        binding.activityChatIvBack.setOnClickListener(v -> onBackPressed());
        binding.activityChatFlSend.setOnClickListener(v -> sendMessage(null));
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
                    binding.activityChatIvSend.setColorFilter(getColor(R.color.colorSentIcon));
                    binding.activityChatFlSend.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.activityChatIvSelectImage.setOnClickListener(v -> {
            getImage.launch("image/*");
        });
        binding.activityChatTvName.setOnClickListener(v -> binding.activityChatFlEmoteList.setVisibility(View.GONE));
        binding.activityChatIvSelectEmote.setOnClickListener(v -> {
            if (binding.activityChatFlEmoteList.getVisibility() == View.VISIBLE) {
                binding.activityChatFlEmoteList.setVisibility(View.GONE);
            } else if (binding.activityChatFlEmoteList.getVisibility() == View.GONE) {
                binding.activityChatFlEmoteList.setVisibility(View.VISIBLE);
                for (int index = 1; index <= 6; index++) {
                    String imagePath = "stickers/" + index + ".png";
                    StorageReference reference = FirebaseStorage.getInstance().getReference(imagePath);
                    File tempFile;
                    try {
                        tempFile = File.createTempFile("tempFile", ".jpg");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    int finalIndex = index;
                    reference.getFile(tempFile).addOnCompleteListener(task -> {
                        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                        ImageView targetImageView = (ImageView) binding.activityChatFlEmoteList.getChildAt(finalIndex - 1);
                        if (targetImageView != null) {
                            targetImageView.setImageBitmap(bitmap);
                        }
                    });
                }
                binding.emote1.setOnClickListener(v1 -> sendMessage("stickers/1.png"));
                binding.emote2.setOnClickListener(v1 -> sendMessage("stickers/2.png"));
                binding.emote3.setOnClickListener(v1 -> sendMessage("stickers/3.png"));
                binding.emote4.setOnClickListener(v1 -> sendMessage("stickers/4.png"));
                binding.emote5.setOnClickListener(v1 -> sendMessage("stickers/5.png"));
                binding.emote6.setOnClickListener(v1 -> sendMessage("stickers/6.png"));
            }
        });
    }

    private Bitmap getProfileImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
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
                currentUserId,
                this
        );
        binding.activityChatRvMessage.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        binding.activityChatIvProfileImage.setImageBitmap(getProfileImage(receivedUser.getImage()));
    }

    private void sendMessage(String imagePath) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, currentUserId);
        message.put(Constants.KEY_RECEIVER_ID, receivedUser.getId());
        if (imagePath != null) {
            message.put(Constants.KEY_IMAGE_MESSAGE, imagePath);
        } else {
            message.put(Constants.KEY_MESSAGE, binding.activityChatEtMessage.getText().toString());
        }
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_MESSAGES).add(message);
        if (conversationId != null) {
            if (imagePath != null) {
                updateConversation("[Image]");
            } else {
                updateConversation(binding.activityChatEtMessage.getText().toString());
            }
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, currentUserId);
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getData(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getData(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receivedUser.getId());
            conversation.put(Constants.KEY_RECEIVER_NAME, receivedUser.getName());
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receivedUser.getImage());
            if (imagePath != null) {
                conversation.put(Constants.KEY_LAST_MESSAGE, "[Image]");
            } else {
                conversation.put(Constants.KEY_LAST_MESSAGE, binding.activityChatEtMessage.getText().toString());
            }
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
        if (!isOnline) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receivedUser.getFcmToken());

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, currentUserId);
                data.put(Constants.KEY_NAME, preferenceManager.getData(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getData(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.activityChatEtMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        binding.activityChatEtMessage.setText(null);
    }

    private String formatDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm:a", Locale.getDefault()).format(date);
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_MESSAGES)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_MESSAGES)
                .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .addSnapshotListener(eventListener);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkConversation() {
        if (messages.size() > 0) {
            checkConversationFromFirestore(currentUserId, receivedUser.getId());
            checkConversationFromFirestore(receivedUser.getId(), currentUserId);
        }
    }

    private void checkConversationFromFirestore(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private void listenUserAvailability() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receivedUser.getId())
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        receivedUser.setFcmToken(value.getString(Constants.KEY_FCM_TOKEN));
                        if (Boolean.TRUE.equals(value.getBoolean(Constants.KEY_USER_AVAILABILITY))) {
                            binding.activityChatIvOnline.setVisibility(View.VISIBLE);
                            isOnline = true;
                        } else {
                            binding.activityChatIvOnline.setVisibility(View.INVISIBLE);
                            binding.activityChatTvOnline.setText(R.string.offline);
                            binding.activityChatTvOnline.setTextColor(getResources().getColor(R.color.colorOffline));
                            isOnline = false;
                        }
                        if (receivedUser.getImage() == null) {
                            receivedUser.setImage(value.getString(Constants.KEY_IMAGE));
                            chatAdapter.setReceiverProfileImage(getProfileImage(receivedUser.getImage()));
                            chatAdapter.notifyItemRangeInserted(0, messages.size());
                        }
                    }
                });
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenUserAvailability();
    }

    @Override
    public void onHoldListener(Message message, int position) {
        PopupWindow popupWindow;
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_menu_message_item, findViewById(R.id.layout_delete));
        popupWindow = new PopupWindow(view, 350, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAsDropDown(Objects.requireNonNull(binding.activityChatRvMessage.findViewHolderForAdapterPosition(position)).itemView.findViewById(R.id.item_container_sent_message_tvMessage), -50, -60);
    }
}