package com.wifimingle.thread;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.wifimingle.R;
import com.wifimingle.activity.ActivitySingleChat;
import com.wifimingle.async.DefaultDiscovery;
import com.wifimingle.interfaces.PublishHostInterface;
import com.wifimingle.model.ChatMessageModel;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.Message;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.wifimingle.Utils.Utilities.getCurrentTimeStamp;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST_INCOMING;
import static com.wifimingle.constants.Constants.APP_MINGLER_IMAGE_FOLDER;
import static com.wifimingle.constants.Constants.APP_NAME;

/**
 * Created by BrOlLy on 22/12/2017.
 */

public class ChatServerForImage extends Thread {
    private String folder_main = "WifiMingle";
    private ServerSocket serverSocket;
    private String cameraEmojiCode = "\uD83D\uDCF7";
    private DefaultDiscovery defaultDiscovery;
    private PublishHostInterface publishHostInterface;

    private Context context;
    private int SocketServerPORT = 8000;

    public ChatServerForImage(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Socket socket = null;
        String imagePath = null;

        try {
            byte[] mmBuffer = new byte[8192];
            byte[] completeMessage = new byte[0];

            int numBytes; // bytes returned from read()
            boolean flag = true;
            int actualLength = 0;
            long total = 0;
            String length = "";
            serverSocket = new ServerSocket(SocketServerPORT);
            socket = serverSocket.accept();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            //ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            //InputStream inputStream = socket.getInputStream();
            while (true) {
                try {
                    // Read from the InputStream.
                    //numBytes = inputStream.read(mmBuffer);
                    numBytes = dataInputStream.read(mmBuffer);
                    if (numBytes != 0) {
                        if (flag) {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < mmBuffer.length; i++) {
                                if (mmBuffer[i] == 126) {
                                    flag = false;
                                    break;
                                } else {
                                    builder.append(Character.toString((char) mmBuffer[i]));
                                }
                            }
                        }
                        byte[] exTemp = new byte[mmBuffer.length];
                        System.arraycopy(mmBuffer, 0, exTemp, 0, mmBuffer.length);

                        byte[] tempBytes = completeMessage;
                        completeMessage = new byte[tempBytes.length + numBytes];
                        System.arraycopy(tempBytes, 0, completeMessage, 0, tempBytes.length);
                        System.arraycopy(mmBuffer, 0, completeMessage, tempBytes.length, numBytes);

                        if (completeMessage[completeMessage.length - 1] == 126 && completeMessage[completeMessage.length - 2] == 126
                                && completeMessage[completeMessage.length - 3] == 126 && completeMessage[completeMessage.length - 4] == 126
                                && completeMessage[completeMessage.length - 5] == 126) {
                            /*String str = new String(completeMessage);
                            if(str.substring(0, 6).equals("iphone")){
                                savingMessageFromIOS(completeMessage);
                            }else {
                                savingMultimediaMessage(completeMessage);
                            }*/
                            savingMultimediaMessage(completeMessage);

                            completeMessage = new byte[0];
                            break;
                        } else if (completeMessage.length == 1 && completeMessage[0] == 125) {
                            completeMessage = new byte[0];
                        }
                    }else {
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void savingMultimediaMessage(byte[] msgArray) {
        try {
            JSONObject jsonObject = new JSONObject(new String(msgArray));
            String msgString = jsonObject.get("message_json").toString();
            String hostBeanString = jsonObject.get("hostbean_json").toString();
            String registrationString = jsonObject.get("registration_json").toString();

            saveMessageAndInsertImageInFiles(msgString, hostBeanString, registrationString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendLocalBroadCast(Context context, Message message, int notificationId) {
        Intent intentBroadcast = new Intent(INTENT_FILTER_BROADCAST_INCOMING);
        if (message != null) {
            message.client_server = "server";
            intentBroadcast.putExtra("data_message", message);
            intentBroadcast.putExtra("notification_id", notificationId);
        }
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intentBroadcast);
    }

    private void sendLocalBroadCastForNewMingler(Context context, String hostString, String regString) {
        Intent intentBroadcast = new Intent(INTENT_FILTER_BROADCAST);
        intentBroadcast.putExtra("reg_String", regString);
        intentBroadcast.putExtra("host_String", hostString);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intentBroadcast);
    }

    private void buildingNotification(Context context, String title, String message, PendingIntent pendingIntent, int notificationId) {
        int icon = R.mipmap.launcher_icon;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText(cameraEmojiCode + " Photo");
            bigText.setBigContentTitle(title);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(icon)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setContentText(cameraEmojiCode + " Photo")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setStyle(bigText);

            builder.setAutoCancel(true);
            notificationManager.notify(notificationId, builder.build());
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle(context.getResources().getString(R.string.app_name))
                    .setContentText(cameraEmojiCode + " " + message)
                    .setSmallIcon(icon)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent).setSound(uri);

            notificationManager.notify(notificationId, builder.build());
        }
    }

    private PendingIntent getPendingIntent(Context context, int notificationId, HostBean hostBean, Message message) {
        Intent intent1 = new Intent(context, ActivitySingleChat.class);
        intent1.putExtra("host", hostBean);
        message.client_server = "server";
        intent1.putExtra("message", message);
        return PendingIntent.getActivity(context, notificationId, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void saveMessageAndInsertImageInFiles(String msgString, String hostBeanString, String registrationString){
        Gson gson = new Gson();
        Message msg = gson.fromJson(msgString, Message.class);
        HostBean hostBean = gson.fromJson(hostBeanString, HostBean.class);
        ChatMessageModel chatMessageModel = insertDataFromSrver(msg);

        String phoneNumber = msg.phone;
        sendLocalBroadCastForNewMingler(context, hostBeanString, registrationString);
        int notificationId = Integer.valueOf(phoneNumber.substring(phoneNumber.length() - 7));
        saveImageInExternalStorage(chatMessageModel, msg, hostBean, notificationId, context);
    }

    private ChatMessageModel insertDataFromSrver(Message message) {
        ChatMessageModel chatMessageModel = new ChatMessageModel();
        chatMessageModel.chatMessage = message.message;
        chatMessageModel.date = message.time;
        chatMessageModel.from_client_server = "server";
        chatMessageModel.ip = message.ip;
        chatMessageModel.name = message.name;
        chatMessageModel.onlineStatus = message.OnlineStatus;
        chatMessageModel.phoneNumber = message.phone;
        return chatMessageModel;
    }

    private void saveImageInExternalStorage(ChatMessageModel chatMessageModel, Message msg, HostBean hostBean, int notificationId, Context context){
        SavePhotoTask savePhotoTask = new SavePhotoTask(chatMessageModel, msg, hostBean, notificationId, context);
        byte[] imgByte = Base64.decode(msg.imageString, Base64.DEFAULT);
        savePhotoTask.execute(imgByte);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SavePhotoTask extends AsyncTask<byte[], String, String> {

        private Context context;
        private ChatMessageModel chatMessageModel;
        private Message message;
        private HostBean hostBean;
        private int notificationId;



        public SavePhotoTask(ChatMessageModel chatMessageModel, Message message, HostBean hostBean, int notificationId, Context context) {
            this.chatMessageModel = chatMessageModel;
            this.message = message;
            this.hostBean = hostBean;
            this.notificationId = notificationId;
            this.context = context;
        }

        @Override
        protected String doInBackground(byte[]... jpeg) {
            File photo = new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME, APP_MINGLER_IMAGE_FOLDER );
            if (!photo.exists()) {
                photo.mkdirs();
            }

            try {
                File photoPath = new File(photo.getPath() + File.separator + "IMG-" + getCurrentTimeStamp() + ".jpeg");
                photoPath.createNewFile();
                FileOutputStream fos = new FileOutputStream(photoPath);

                fos.write(jpeg[0]);
                fos.close();

                return photoPath.getPath();
            } catch (java.io.IOException e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            chatMessageModel.imagePath = s;
            chatMessageModel.save();

            buildingNotification(context, message.name, message.message, getPendingIntent(context, notificationId, hostBean, message), notificationId);
            sendLocalBroadCast(context, message, notificationId);
        }
    }
}


