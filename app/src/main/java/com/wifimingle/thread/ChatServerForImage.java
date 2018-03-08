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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.wifimingle.Utils.Utilities.getCurrentTimeStamp;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST_INCOMING;
import static com.wifimingle.constants.Constants.APP_MINGLER_IMAGE_FOLDER;
import static com.wifimingle.constants.Constants.APP_MINGLER_IMAGE_SENT_FOLDER;
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
        /*defaultDiscovery = new DefaultDiscovery(activity);
        setListener();
        createFolderForImages();*/
    }

    private void createFolderForImages() {
        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    private String inserImageInThatFolder() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String currentTimeStamp = dateFormat.format(new Date());
        File f1 = new File(Environment.getExternalStorageDirectory() + "/" + folder_main + "/", "IMG" + currentTimeStamp + ".jpg");
        if (!f1.exists()) {
            f1.mkdirs();
            return f1.getAbsolutePath();
        } else
            return f1.getAbsolutePath();
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
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = objectInputStream.read(mmBuffer);
                    /*byte[] mBuffer = new byte[2024];
                    mmInStream.read(mBuffer);*/
                    if (numBytes != 0) {
                        if (flag) {
                            StringBuilder builder = new StringBuilder();
                            //String[] data = new String[]{mmBuffer.toString()};
                            for (int i = 0; i < mmBuffer.length; i++) {
                                if (mmBuffer[i] == 126) {
                                    flag = false;
                                    break;
                                } else {
                                    builder.append(Character.toString((char) mmBuffer[i]));
                                    length = builder.toString();
                                    //actualLength = Integer.valueOf(length);
                                    //actualLength = actualLength + Integer.valueOf(mmBuffer[i]);
                                }
                            }
                        }
                        byte[] exTemp = new byte[mmBuffer.length];
                        System.arraycopy(mmBuffer, 0, exTemp, 0, mmBuffer.length);
                        String newStr = new String(exTemp);
                        /*total += Integer.valueOf(length);
                        mBuilder.setContentTitle("Downloading data")
                                .setContentText("Download in progress")
                                .setSmallIcon(R.mipmap.ic_launcher);

                        mBuilder.setProgress(100, (int) (total * 100 / actualLength), false);
                        mNotifyManager.notify(MainActivity.id, mBuilder.build());*/
                        /*try {
                            // Sleep for 5 seconds
                            Thread.sleep(1*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        byte[] tempBytes = completeMessage;
                        completeMessage = new byte[tempBytes.length + numBytes];
                        System.arraycopy(tempBytes, 0, completeMessage, 0, tempBytes.length);
                        System.arraycopy(mmBuffer, 0, completeMessage, tempBytes.length, numBytes);
                        //Log.i("bluetooth bytes","Copying data "+String.valueOf(numBytes));
                        if (completeMessage[completeMessage.length - 1] == 126 && completeMessage[completeMessage.length - 2] == 126
                                && completeMessage[completeMessage.length - 3] == 126 && completeMessage[completeMessage.length - 4] == 126
                                && completeMessage[completeMessage.length - 5] == 126) {

                            savingMultimediaMessage(completeMessage);
                            /*mmOutStream.write(ackByte);
                            android.os.Message readMsg = mHandler.obtainMessage(
                                    Constants.MESSAGE_TOAST_RECIEVED_ACKNOWLEDGMENT, numBytes, -1,
                                    completeMessage);
                            readMsg.sendToTarget();
                            completeMessage = Arrays.copyOfRange(completeMessage, 0, completeMessage.length - 5);
                            //String temp is the complete msg.
                            String temp = new String(completeMessage);
                            //connectionLost();
                            byte[] tempByte = Base64.decode(temp,0);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(tempByte , 0, tempByte.length);
                            //SaveImage(bitmap);
                            mBuilder.setContentText("Download complete")
                                    // Removes the progress bar
                                    .setProgress(0,0,false);
                            mNotifyManager.notify(MainActivity.id, mBuilder.build());

                            Log.i("bluetooth bytes", temp.length() + " writing ack " + temp);*/
                            completeMessage = new byte[0];
                            break;
                        } else if (completeMessage.length == 1 && completeMessage[0] == 125) {
                            /*android.os.Message readMsg = mHandler.obtainMessage(
                                    Constants.MESSAGE_TOAST_SENT_ACKNOWLEDGMENT, numBytes, -1,
                                    completeMessage);
                            readMsg.sendToTarget();*/

                            //Log.i("bluetooth bytes","inside ack");
                            completeMessage = new byte[0];
                        }
                    }else {
                        /*savingMultimediaMessage(completeMessage);
                        completeMessage = new byte[0];*/
                        break;
                    }
                    /*try {
                        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                        int bytesRead;
                        int current = 0;
                        //int fileSize = inputStream.read();
                        int fileSize = 150000;
                        //int filesize=65383;
                        byte [] mybytearray2  = new byte [fileSize];

                        *//*imagebyte = inserImageInThatFolder();

                        FileOutputStream fos = new FileOutputStream(imagebyte);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        bytesRead = is.read(mybytearray2,0,mybytearray2.length);
                        current = bytesRead;*//*
                        do {
                            bytesRead = inputStream.read(mybytearray2, current, (mybytearray2.length-current));
                            if(bytesRead >= 0) current += bytesRead;
                        } while(bytesRead > -1);
                        savingMultimediaMessage(mybytearray2);
                        *//*bos.write(mybytearray2, 0 , current);
                        bos.flush();

                        bos.close();*//*

                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/

                } catch (Exception e) {
                    e.printStackTrace();
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
        //chatMessageModel.imagePath = message.imagebyte;
        chatMessageModel.date = message.time;
        chatMessageModel.from_client_server = "server";
        chatMessageModel.ip = message.ip;
        chatMessageModel.name = message.name;
        chatMessageModel.onlineStatus = message.OnlineStatus;
        chatMessageModel.phoneNumber = message.phone;
        return chatMessageModel;
        //chatMessageModel.save();
    }

    private void saveImageInExternalStorage(ChatMessageModel chatMessageModel, Message msg, HostBean hostBean, int notificationId, Context context){
        SavePhotoTask savePhotoTask = new SavePhotoTask(chatMessageModel, msg, hostBean, notificationId, context);
        savePhotoTask.execute(msg.imagebyte);
        /*final String imagePath;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File photo = new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME + "/" + APP_MINGLER_IMAGE_FOLDER, "IMG-" + getCurrentTimeStamp() + ".jpeg");
                if (!photo.exists()) {
                    photo.mkdirs();
                }
                try {
                    imagePath = photo.getAbsolutePath();
                    FileOutputStream fos = new FileOutputStream(photo.getPath());

                    fos.write(imgByte);
                    fos.close();
                }
                catch (IOException e) {
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }
            }
        });*/
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
            //File photo = new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME + "/" + APP_MINGLER_IMAGE_FOLDER, "IMG-" + getCurrentTimeStamp() + ".jpeg");
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


