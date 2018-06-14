package com.wifimingle.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.wifimingle.R;
import com.wifimingle.activity.ActivityMain;
import com.wifimingle.activity.ActivitySingleChat;
import com.wifimingle.constants.Constants;
import com.wifimingle.model.ChatMessageModel;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.Message;
import com.wifimingle.model.RegistrationModel;
import com.wifimingle.thread.ChatClientFromService;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST_INCOMING;
import static com.wifimingle.activity.ActivityMain.TAG;
import static com.wifimingle.activity.ActivitySingleChat.SEEN;


public class ListeningForOnlineStatus extends Service {

    private ChatServerThread chatServerThread;
    //private final int SocketServerPORT = 8080;
    private final int SocketServerPORT = 53705;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ListenService onDestroy called");
        if (chatServerThread != null) {
            chatServerThread.interrupt();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        chatServerThread = new ChatServerThread(this);
        chatServerThread.start();
        Log.e(TAG, "ListenService OnStartCommand called");
        return START_STICKY;
    }

    private class ChatServerThread extends Thread {

        ConnectThread connectThread = null;
        private ServerSocket serverSocket;
        private Context context;

        public ChatServerThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                while (true) {
                    try {
                        socket = serverSocket.accept();
                        /*if(connectThread != null){
                            connectThread.setDataInputStream(socket);
                            Log.e("ListenInsideIf", "setDataInputStream method calls");
                        }else {
                            Log.e("ListenInsideIf", "connect thread initializes");
                            connectThread = new ConnectThread(socket, context);
                            connectThread.start();
                        }*/
                        connectThread = new ConnectThread(socket, context);
                        connectThread.start();
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

        public void sendMsgServer(String s) {
            if (connectThread != null) {
                connectThread.sendMsg(s);
            }
        }
    }

    private class ConnectThread extends Thread {

        Socket socket;
        Context context;
        String msgToSend = "";
        String serverName;
        String clientIMEI;
        boolean sendCheck = false;
        //DataInputStream dataInputStream = null;
        ObjectInputStream dataInputStream = null;

        ConnectThread(Socket socket, Context context) {
            this.socket = socket;
            this.context = context;
            serverName = "";
        }

        private void setDataInputStream(Socket dataInputStreamSocket){
            try {
                //dataInputStream = new DataInputStream(dataInputStreamSocket.getInputStream());
                dataInputStream = new ObjectInputStream(dataInputStreamSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            /*InputStream inputStream = null;
            OutputStream outputStream = null;*/
            //recieveMessageMechanismForIphone();
            //tring recievedData = "";
            String newMsg = recieveMessageMechanismForIphone(socket);

            if (!newMsg.equals("")) {
                newMsg = newMsg.replace("~", "").replace("~", "").replace("~", "")
                        .replace("~", "").replace("~", "");

                if (newMsg.substring(0, 2).equals("$h")) {
                    chatClientForPingResponse(newMsg);
                }else if (newMsg.substring(0, 7).equals("$status")) {
                    sendLocalBroadCastForStatusChange(context, newMsg);
                } else if (newMsg.substring(0, 8).equals("$Welcome")) {

                    try {
                        JSONObject jsonObject = new JSONObject(newMsg.substring(newMsg.indexOf(",") + 1));
                        String msgString = jsonObject.get("message_json").toString();
                        String hostBeanString = jsonObject.get("hostbean_json").toString();

                        Gson gson = new Gson();
                        Message msg = gson.fromJson(msgString, Message.class);
                        HostBean hostBean = gson.fromJson(hostBeanString, HostBean.class);
                        insertDataFromSrver(msg);
                        String phoneNumber = msg.phone;
                        int id = Integer.valueOf(phoneNumber.substring(phoneNumber.length() - 7));
                        buildingNotification(context, msg.name, msg.message, getPendingIntent(context, id, hostBean, msg), id);
                        sendLocalBroadCast(context, msg, id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //buildingNotification(context, "Welcome Message", "Hello new Mingler", getPendingIntentForWelcomeMessage(context, id), id);
                }else if (newMsg.substring(0, 15).equals("$acknowledgment")) {
                    String phone = newMsg.substring(15);
                    seenMessageInDb(phone);
                    sendLocalBroadCastForAcknowledge(context);
                }else if (!newMsg.equals("") && !newMsg.substring(0, 2).equals("$h") && !newMsg.substring(0, 7).equals("$status") && !newMsg.substring(0, 8).equals("$Welcome") && !newMsg.substring(0, 15).equals("$acknowledgment")) {
                    //int notificationId = NotificationID.getID();
                    try {
                        JSONObject jsonObject = new JSONObject(newMsg);
                        String msgString = jsonObject.get("message_json").toString();
                        String hostBeanString = jsonObject.get("hostbean_json").toString();
                        String registrationString = jsonObject.get("registration_json").toString();

                        Gson gson = new Gson();
                        Message msg = gson.fromJson(msgString, Message.class);
                        HostBean hostBean = gson.fromJson(hostBeanString, HostBean.class);
                        insertDataFromSrver(msg);
                        String phoneNumber = msg.phone;
                        sendLocalBroadCastForNewMingler(context, hostBeanString, registrationString);
                        int notificationId = Integer.valueOf(phoneNumber.substring(phoneNumber.length() - 7));
                        buildingNotification(context, msg.name, msg.message, getPendingIntent(context, notificationId, hostBean, msg), notificationId);
                        sendLocalBroadCast(context, msg, notificationId);
                        //buildingNotification(context, "title", newMsg, getPendingIntent(context, notificationId, new HostBean(), new Message()), notificationId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            /*try {
                dataInputStream = new DataInputStream(socket.getInputStream());

                while (true) {
                    if (dataInputStream.available() > 0) {
                        Log.e("ListenInsideIf", "date input stream is available method calls");
                        String newMsg = dataInputStream.readUTF();
                        Log.e("ClientServer", newMsg);
                        if (newMsg.substring(0, 2).equals("$h")) {
                            chatClientForPingResponse(newMsg);

                        } else if (newMsg.substring(0, 7).equals("$status")) {
                            sendLocalBroadCastForStatusChange(context, newMsg);
                        } else if (newMsg.substring(0, 8).equals("$Welcome")) {

                            try {
                                JSONObject jsonObject = new JSONObject(newMsg.substring(newMsg.indexOf(",") + 1));
                                String msgString = jsonObject.get("message_json").toString();
                                String hostBeanString = jsonObject.get("hostbean_json").toString();

                                Gson gson = new Gson();
                                Message msg = gson.fromJson(msgString, Message.class);
                                HostBean hostBean = gson.fromJson(hostBeanString, HostBean.class);
                                insertDataFromSrver(msg);
                                String phoneNumber = msg.phone;
                                int id = Integer.valueOf(phoneNumber.substring(phoneNumber.length() - 7));
                                buildingNotification(context, msg.name, msg.message, getPendingIntent(context, id, hostBean, msg), id);
                                sendLocalBroadCast(context, msg, id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //buildingNotification(context, "Welcome Message", "Hello new Mingler", getPendingIntentForWelcomeMessage(context, id), id);
                        } else if (newMsg.substring(0, 15).equals("$acknowledgment")) {
                            String phone = newMsg.substring(15);
                            seenMessageInDb(phone);
                            sendLocalBroadCastForAcknowledge(context);
                        }else if (!newMsg.equals("") && !newMsg.substring(0, 2).equals("$h") && !newMsg.substring(0, 7).equals("$status") && !newMsg.substring(0, 8).equals("$Welcome") && !newMsg.substring(0, 15).equals("$acknowledgment")) {
                            //int notificationId = NotificationID.getID();
                            try {
                                JSONObject jsonObject = new JSONObject(newMsg);
                                String msgString = jsonObject.get("message_json").toString();
                                String hostBeanString = jsonObject.get("hostbean_json").toString();
                                String registrationString = jsonObject.get("registration_json").toString();

                                Gson gson = new Gson();
                                Message msg = gson.fromJson(msgString, Message.class);
                                HostBean hostBean = gson.fromJson(hostBeanString, HostBean.class);
                                insertDataFromSrver(msg);
                                String phoneNumber = msg.phone;
                                sendLocalBroadCastForNewMingler(context, hostBeanString, registrationString);
                                int notificationId = Integer.valueOf(phoneNumber.substring(phoneNumber.length() - 7));
                                buildingNotification(context, msg.name, msg.message, getPendingIntent(context, notificationId, hostBean, msg), notificationId);
                                sendLocalBroadCast(context, msg, notificationId);
                                //buildingNotification(context, "title", newMsg, getPendingIntent(context, notificationId, new HostBean(), new Message()), notificationId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            break;

                        //broadcastMsg(n + ": " + newMsg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                serverName = "";
                msgToSend = "";
            }*/
        }

        private void chatClientForPingResponse(String message) {
            String hostBeanString = message.substring(message.indexOf(",") + 1);
            String ipAddressOfClientDevice = message.substring(2, message.indexOf(","));

            RegistrationModel registrationModel = RegistrationModel.first(RegistrationModel.class);

            ChatClientFromService chatClientFromService = new ChatClientFromService(ipAddressOfClientDevice);
            if (registrationModel != null) {
                registrationModel.setProfilePicString("");
                String registrationModelString = new Gson().toJson(registrationModel);
                chatClientFromService.start();
                chatClientFromService.sendMsg("$s" + hostBeanString + ";" + registrationModelString + Constants.HEADER);
                //chatClientFromService.disconnect();
                chatClientFromService.interrupt();
            } else {
                chatClientFromService.interrupt();
            }
        }

        private boolean sendChk() {
            return sendCheck;
        }

        private void sendMsg(String msg) {
            msgToSend = msg;
        }

        private void insertDataFromSrver(Message message) {
            ChatMessageModel chatMessageModel = new ChatMessageModel();
            chatMessageModel.chatMessage = message.message;
            chatMessageModel.date = message.time;
            chatMessageModel.from_client_server = "server";
            chatMessageModel.ip = message.ip;
            chatMessageModel.name = message.name;
            chatMessageModel.onlineStatus = message.OnlineStatus;
            chatMessageModel.phoneNumber = message.phone;
            chatMessageModel.save();
        }
    }

    private void seenMessageInDb(String phoneNumber){
        ArrayList<ChatMessageModel> msgs = (ArrayList<ChatMessageModel>) ChatMessageModel.find(ChatMessageModel.class, "phone_Number = ?", phoneNumber);
        if(msgs.size() > 0){
            for (ChatMessageModel m: msgs) {
                if(m.from_client_server.equals("client")){
                    m.seen_unseen = SEEN;
                    ChatMessageModel.save(m);
                }
            }
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

    private void sendLocalBroadCastForAcknowledge(Context context) {
        Intent intentBroadcast = new Intent(INTENT_FILTER_BROADCAST_INCOMING);
        //intentBroadcast.putExtra("acknowledge", "acknowledge");
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intentBroadcast);
    }

    private void sendLocalBroadCastForStatusChange(Context context, String message) {
        Intent intentBroadcast = new Intent(INTENT_FILTER_BROADCAST);
        String status = message.substring(7, message.indexOf(","));
        String hostIp = message.substring(message.indexOf(",") + 1);
        intentBroadcast.putExtra("status", status);
        intentBroadcast.putExtra("host_ip_String", hostIp);
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
            bigText.bigText(message);
            bigText.setBigContentTitle(title);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(icon)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setContentText(message)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setStyle(bigText);

            builder.setAutoCancel(true);
            notificationManager.notify(notificationId, builder.build());
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(message)
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

    private PendingIntent getPendingIntentForWelcomeMessage(Context context, int notificationId) {
        Intent intent1 = new Intent(context, ActivityMain.class);
        return PendingIntent.getActivity(context, notificationId, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void gettingByteArrayMessage(byte[] msg){

    }

    private String recieveMessageMechanismForIphone(Socket socket){
        //Socket socket = null;
        DataInputStream dataInputStream = null;
        try {
            byte[] mmBuffer = new byte[2136];
            byte[] completeMessage = new byte[0];

            int numBytes; // bytes returned from read()
            boolean flag = true;
            int actualLength = 0;
            long total = 0;
            String length = "";

            //ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
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
                            //savingMultimediaMessage(completeMessage);
                            return new String(completeMessage);
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
            if(dataInputStream != null){
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
