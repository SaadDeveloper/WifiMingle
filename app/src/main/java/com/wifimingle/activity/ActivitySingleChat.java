package com.wifimingle.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wifimingle.R;
import com.wifimingle.Utils.Utilities;
import com.wifimingle.adapter.SingleChatListAdapter;
import com.wifimingle.constants.Constants;
import com.wifimingle.model.ChatMessageModel;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.Message;
import com.wifimingle.model.RegistrationModel;
import com.wifimingle.thread.ChatClient;
import com.wifimingle.thread.ChatClientForImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.wifimingle.constants.Constants.APP_MINGLER_IMAGE_FOLDER;
import static com.wifimingle.constants.Constants.APP_MINGLER_IMAGE_SENT_FOLDER;
import static com.wifimingle.constants.Constants.APP_NAME;
import static com.wifimingle.constants.Constants.STORAGE;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST_INCOMING;

public class ActivitySingleChat extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private EditText etMessageArea;
    private ImageButton btnSend;
    private ImageButton back;
    private ImageButton btnAttachment;
    private HostBean hostBean;
    private Message message;
    private RegistrationModel reg;
    private TextView personName;
    private TextView personStatus;
    private ListView messagesList;
    private SingleChatListAdapter singleChatListAdapter;
    public final static String FORMAT_DATE_TIME = "dd-MM-yyyy hh:mm a";
    public final static String SEEN = "seen";
    public final static String UNSEEN = "unseen";

    private ArrayList<ChatMessageModel> messageList;
    private int REQUEST_IMAGE_OPEN = 123;
    private final int PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.dark_gray));
        }
        setContentView(R.layout.activity_single_chat);
        try {
            init();
            fetchData();
            initClickListners();
            if (hostBean.onlineStatus) {
                sendMessage(hostBean, "$acknowledgment" + reg.phone);
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(Integer.valueOf(hostBean.phoneNumber.substring(hostBean.phoneNumber.length() - 7)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        etMessageArea = findViewById(R.id.message_body_field);
        btnSend = findViewById(R.id.sendButton_sim1);
        btnAttachment = findViewById(R.id.img_btn_attach);
        personStatus = findViewById(R.id.tv_chat_status);
        messagesList = findViewById(R.id.listMessages);
        personName = findViewById(R.id.tv_chat_name);
        back = findViewById(R.id.imgBtn_back);
        reg = RegistrationModel.first(RegistrationModel.class);
        reg.setProfilePicString("");
    }

    private void fetchData() {
        hostBean = (HostBean) getIntent().getParcelableExtra("host");
        String hostName = (String) getIntent().getStringExtra("host_name");
        hostBean.phoneNumber = (String) getIntent().getStringExtra("host_phone");
        hostBean.onlineStatus = (Boolean) getIntent().getBooleanExtra("host_status", true);
        if (hostName != null) {
            hostBean.deviceName = hostName;
            message = (Message) getIntent().getSerializableExtra("message");
            personName.setText(hostName);
            if (hostBean.onlineStatus) {
                personStatus.setText("Online");
            } else {
                personStatus.setText("Offline");
            }
        } else {
            message = (Message) getIntent().getSerializableExtra("message");
            hostBean.ipAddress = message.ip;
            hostBean.phoneNumber = message.phone;
            personName.setText(message.name);
        }
    }

    private void initClickListners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
            }
        });

        btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    methodRequiresPermission();
                } else {
                    selectImage();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_OPEN);
    }

    private void populateChat() {
        messageList = new ArrayList<>();
        messageList = (ArrayList<ChatMessageModel>) ChatMessageModel.find(ChatMessageModel.class, "phone_Number = ?", hostBean.phoneNumber);

        if (messageList != null && messagesList.getAdapter() != null) {
            singleChatListAdapter.setUpdatedList(messageList, personName.getText().toString());
            ((SingleChatListAdapter) messagesList.getAdapter()).notifyDataSetChanged();
        } else {
            singleChatListAdapter = new SingleChatListAdapter(getApplicationContext(), this, messageList, personName.getText().toString());
            messagesList.setAdapter(singleChatListAdapter);
            singleChatListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(INTENT_FILTER_BROADCAST_INCOMING));
            populateChat();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_OPEN && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            File actualImage = new File("");
            try {
                if (fullPhotoUri != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        actualImage = new File(getRealPathFromURI(getApplicationContext(), fullPhotoUri));
                    } else {
                        actualImage = new File(getRealPathFromURI(fullPhotoUri));
                    }
                    long length = actualImage.length() / 1024;
                    if (length > 500 && length < 1024) {
                        File compressedImage = new Compressor(ActivitySingleChat.this)
                                .setMaxWidth(640)
                                .setMaxHeight(480)
                                .setQuality(60)
                                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                .setDestinationDirectoryPath(new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME + "/" + APP_MINGLER_IMAGE_FOLDER, APP_MINGLER_IMAGE_SENT_FOLDER).getAbsolutePath())
                                .compressToFile(actualImage);

                        Uri comressedImageUri = Uri.fromFile(compressedImage);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), comressedImageUri);
                        byte[] byteArray = Utilities.getBytes(bitmap);
                        insertImageAndSend(byteArray, compressedImage.getAbsolutePath());
                    }else if(length > 1024 && length < 1600){
                        File compressedImage = new Compressor(ActivitySingleChat.this)
                                .setMaxWidth(640)
                                .setMaxHeight(480)
                                .setQuality(50)
                                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                .setDestinationDirectoryPath(new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME + "/" + APP_MINGLER_IMAGE_FOLDER, APP_MINGLER_IMAGE_SENT_FOLDER).getAbsolutePath())
                                .compressToFile(actualImage);

                        Uri comressedImageUri = Uri.fromFile(compressedImage);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), comressedImageUri);
                        byte[] byteArray = Utilities.getBytes(bitmap);
                        insertImageAndSend(byteArray, compressedImage.getAbsolutePath());
                    }else if(length < 500){
                        File compressedImage = new Compressor(ActivitySingleChat.this)
                                .setMaxWidth(640)
                                .setMaxHeight(480)
                                .setQuality(100)
                                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                .setDestinationDirectoryPath(new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME + "/" + APP_MINGLER_IMAGE_FOLDER, APP_MINGLER_IMAGE_SENT_FOLDER).getAbsolutePath())
                                .compressToFile(actualImage);

                        Uri comressedImageUri = Uri.fromFile(compressedImage);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), comressedImageUri);
                        byte[] byteArray = Utilities.getBytes(bitmap);
                        insertImageAndSend(byteArray, compressedImage.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static String getRealPathFromURI(Context context, Uri imageUri) {
        String wholeID = DocumentsContract.getDocumentId(imageUri);
        String id = wholeID.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        String filePath = "";
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private void insertData() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        Date dateCurrent = new Date();

        String date = sdf.format(dateCurrent);
        String msg = etMessageArea.getText().toString().trim();

        if (msg.length() == 0) {
            Toast.makeText(this, "type Something", Toast.LENGTH_SHORT).show();
        } else {
            etMessageArea.setText("");
            Message message = new Message("client", msg, getLocalIpAddress(), date, reg.name, reg.phone);
            message.OnlineStatus = hostBean.onlineStatus;
            message.message_seen = UNSEEN;

            ChatMessageModel chatMessageModel = new ChatMessageModel();
            chatMessageModel.chatMessage = msg;
            chatMessageModel.date = date;
            chatMessageModel.from_client_server = "client";
            chatMessageModel.ip = hostBean.ipAddress;
            chatMessageModel.seen_unseen = UNSEEN;
            chatMessageModel.name = reg.name;
            chatMessageModel.onlineStatus = hostBean.onlineStatus;
            chatMessageModel.phoneNumber = hostBean.phoneNumber;
            chatMessageModel.save();

            if (hostBean.onlineStatus) {
                JSONObject jsonObject = new JSONObject();
                try {
                    HostBean hostBean1 = new HostBean(hostBean);
                    hostBean1.ipAddress = message.ip;
                    Gson gson = new Gson();
                    jsonObject.put("message_json", gson.toJson(message));
                    jsonObject.put("hostbean_json", gson.toJson(hostBean1));
                    jsonObject.put("registration_json", gson.toJson(reg));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendMessage(hostBean, jsonObject.toString());
            }
            messageList.add(/*message*/chatMessageModel);
            if (messageList != null && messagesList.getAdapter() != null) {
                singleChatListAdapter.setUpdatedList(messageList, personName.getText().toString());
                ((SingleChatListAdapter) messagesList.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    private void insertImageAndSend(byte[] imagebyte, String imagePath) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        Date dateCurrent = new Date();

        String date = sdf.format(dateCurrent);
        String msg = etMessageArea.getText().toString().trim();

        etMessageArea.setText("");
        Message message = new Message("client", msg, getLocalIpAddress(), date, reg.name, reg.phone);
        message.OnlineStatus = hostBean.onlineStatus;
        message.message_seen = UNSEEN;
        message.imagebyte = imagebyte;

        ChatMessageModel chatMessageModel = new ChatMessageModel();
        chatMessageModel.chatMessage = msg;
        chatMessageModel.date = date;
        chatMessageModel.from_client_server = "client";
        chatMessageModel.ip = hostBean.ipAddress;
        chatMessageModel.seen_unseen = UNSEEN;
        chatMessageModel.name = reg.name;
        chatMessageModel.onlineStatus = hostBean.onlineStatus;
        chatMessageModel.phoneNumber = hostBean.phoneNumber;
        chatMessageModel.imagePath = imagePath;
        chatMessageModel.save();

        if (hostBean.onlineStatus) {
            try {
                HostBean hostBean1 = new HostBean(hostBean);
                hostBean1.ipAddress = message.ip;
                JSONObject jsonObject = new JSONObject();
                Gson gson = new Gson();
                jsonObject.put("message_json", gson.toJson(message));
                jsonObject.put("hostbean_json", gson.toJson(hostBean1));
                jsonObject.put("registration_json", gson.toJson(reg));
                String fullMessageString = jsonObject.toString() + Constants.HEADER;

                byte[] fullMessageByteArr = fullMessageString.getBytes();
                sendMessageWithImage(hostBean, fullMessageByteArr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        messageList.add(chatMessageModel);
        if (messageList != null && messagesList.getAdapter() != null) {
            singleChatListAdapter.setUpdatedList(messageList, personName.getText().toString());
            ((SingleChatListAdapter) messagesList.getAdapter()).notifyDataSetChanged();
        }

    }

    public String getLocalIpAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    private void sendMessage(HostBean host, String message) {
        Log.e("message_seen", "Message is being seen");
        ChatClient chatClient = new ChatClient(host.ipAddress.trim());
        chatClient.start();
        chatClient.sendMsg(message + Constants.HEADER);
        chatClient.interrupt();
    }

    private void sendMessageWithImage(HostBean host, byte[] message) {
        Log.e("message_seen", "Message is being seen");
        ChatClientForImage chatClientForImage = new ChatClientForImage(host.ipAddress.trim());
        chatClientForImage.start();
        chatClientForImage.sendImage(message);
        chatClientForImage.interrupt();
    }

    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendMessage(hostBean, "$acknowledgment" + reg.phone);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(Integer.valueOf(hostBean.phoneNumber.substring(hostBean.phoneNumber.length() - 7)));
            populateChat();
        }
    };

    @AfterPermissionGranted(PERMISSION_REQUEST_CODE)
    private void methodRequiresPermission() {
        String[] perms = {STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            selectImage();
        } else {
            EasyPermissions.requestPermissions(this, "Storage Permission is still pending",
                    PERMISSION_REQUEST_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "Permission is Required for Gallery access", Toast.LENGTH_SHORT).show();
    }
}
