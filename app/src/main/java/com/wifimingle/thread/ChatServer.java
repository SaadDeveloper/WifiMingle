package com.wifimingle.thread;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.google.gson.Gson;
import com.wifimingle.async.DefaultDiscovery;
import com.wifimingle.interfaces.PublishHostInterface;
import com.wifimingle.model.ChatMessageModel;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.Message;
import com.wifimingle.model.PhoneModelForWelcomingMessage;
import com.wifimingle.model.RegistrationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;
import static com.wifimingle.activity.ActivitySingleChat.FORMAT_DATE_TIME;

/**
 * Created by BrOlLy on 22/12/2017.
 */

public class ChatServer extends Thread {
    ConnectThread connectThread = null;
    private ServerSocket serverSocket;
    private DefaultDiscovery defaultDiscovery;
    private PublishHostInterface publishHostInterface;

    private Activity activity;
    private int SocketServerPORT = 8080;

    public ChatServer(Activity activity) {
        this.activity = activity;
        defaultDiscovery = new DefaultDiscovery(activity);
        setListener();
    }

    public void setNewAsyncDiscoveryTask(DefaultDiscovery defaultDiscovery) {
        this.defaultDiscovery = defaultDiscovery;
        setListener();
    }

    private void setListener() {
        this.publishHostInterface = defaultDiscovery;
    }

    @Override
    public void run() {
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(SocketServerPORT);
            while (true) {
                try {
                    socket = serverSocket.accept();
                    if(connectThread != null){
                        connectThread.setDataInputStream(socket);
                        Log.e("insideIf", "setDataInputStream method calls");
                    }else {
                        Log.e("insideIf", "connect thread initializes");
                        connectThread = new ConnectThread(socket, activity);
                        connectThread.start();
                    }
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

    private class ConnectThread extends Thread {

        Socket socket;
        Activity activity;
        String msgToSend = "";
        String serverName;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        ConnectThread(Socket socket, Activity activity) {
            this.socket = socket;
            this.activity = activity;
            serverName = "";
        }

        private void setDataInputStream(Socket dataInputStreamSocket){
            try {
                dataInputStream = new DataInputStream(dataInputStreamSocket.getInputStream());
                dataOutputStream = new DataOutputStream(dataInputStreamSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    if (dataInputStream.available() > 0) {
                        Log.e("insideIf", "date input stream is available method calls");
                        String newMsg = dataInputStream.readUTF();
                        if (newMsg.substring(0, 2).equals("$s")) {
                            String hostString = newMsg.substring(2, newMsg.indexOf(";"));
                            Log.e("newMessage", newMsg);
                            Log.e("hostString", hostString);
                            HostBean host = new Gson().fromJson(hostString, HostBean.class);
                            String registrationString = newMsg.substring(newMsg.indexOf(";") + 1);
                            RegistrationModel reg = new Gson().fromJson(registrationString, RegistrationModel.class);

                            host.onlineStatus = true;
                            host.deviceName = reg.name;
                            host.status = reg.status;
                            host.phoneNumber = reg.phone;
                            host.profilePicByte = reg.profilePic;
                            publishHostInterface.publishHost(host);

                            long count = PhoneModelForWelcomingMessage.count(PhoneModelForWelcomingMessage.class);
                            if(count > 0){
                                List<PhoneModelForWelcomingMessage> found = PhoneModelForWelcomingMessage.find(PhoneModelForWelcomingMessage.class, "phone = ?", reg.phone);
                                if (found.size() == 0){
                                    PhoneModelForWelcomingMessage phone = new PhoneModelForWelcomingMessage();
                                    phone.setPhone(reg.phone);
                                    phone.save();
                                    String jsonString = insertData(activity, host);
                                    ChatClient chatClient = new ChatClient(host.ipAddress);
                                    chatClient.start();
                                    chatClient.sendMsg("$Welcome" + "," + jsonString);
                                    chatClient.interrupt();
                                }
                            }else {
                                PhoneModelForWelcomingMessage phone = new PhoneModelForWelcomingMessage();
                                phone.setPhone(reg.phone);
                                phone.save();
                                String jsonString = insertData(activity, host);
                                ChatClient chatClient = new ChatClient(host.ipAddress);
                                chatClient.start();
                                chatClient.sendMsg("$Welcome" + "," + jsonString);
                                chatClient.interrupt();
                            }
                            /*ArrayList<PhoneModelForWelcomingMessage> phoneList = (ArrayList<PhoneModelForWelcomingMessage>) PhoneModelForWelcomingMessage.listAll(PhoneModelForWelcomingMessage.class);
                            if (phoneList.size() > 0) {
                                for (PhoneModelForWelcomingMessage list : phoneList) {
                                    if (!list.phone.equals(reg.phone)) {
                                        PhoneModelForWelcomingMessage phone = new PhoneModelForWelcomingMessage();
                                        phone.setPhone(reg.phone);
                                        phone.save();
                                        String jsonString = insertData(activity, host);
                                        ChatClient chatClient = new ChatClient(host.ipAddress);
                                        chatClient.start();
                                        chatClient.sendMsg("$Welcome" + "," + jsonString);
                                        chatClient.interrupt();
                                        break;
                                    }
                                }
                            } else {
                                PhoneModelForWelcomingMessage phone = new PhoneModelForWelcomingMessage();
                                phone.setPhone(reg.phone);
                                phone.save();
                                String jsonString = insertData(activity, host);
                                ChatClient chatClient = new ChatClient(host.ipAddress);
                                chatClient.start();
                                chatClient.sendMsg("$Welcome" + "," + jsonString);
                                chatClient.interrupt();
                            }*/

                            ArrayList<ChatMessageModel> forUnsentMesageList = (ArrayList<ChatMessageModel>) ChatMessageModel.find(ChatMessageModel.class, "phone_Number = ?", reg.phone);
                            if(forUnsentMesageList.size() > 0){
                                ChatClient chatClient = new ChatClient(host.ipAddress);
                                chatClient.start();
                                for(ChatMessageModel m: forUnsentMesageList){
                                    if(!m.onlineStatus){
                                        Log.e("Error_Hello", "Error Hello message");
                                        chatClient.sendMsg(sendDataOfUnsentMessage(activity, host, m));
                                    }
                                }
                                chatClient.interrupt();
                            }

                            //((TabActivity) activity).setHostBeanListMinglers(host);
                            //publish(host);
                        } else {
                            Log.e("insideIf", "breaking while loop");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Log.e("insideIf", "closing streams");
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                serverName = "";
                msgToSend = "";
            }
        }

        private void sendMsg(String msg) {
            msgToSend = msg;
        }

        private String insertData(Context context, HostBean hostBean) {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
            Date dateCurrent = new Date();

            String date = sdf.format(dateCurrent);
            //String msg = et_message_area.getText().toString().trim();

            RegistrationModel reg = RegistrationModel.first(RegistrationModel.class);
            Message message = new Message("client", "Hello new Mingler", getLocalIpAddress(context), date, reg.name, reg.phone);
            message.OnlineStatus = hostBean.onlineStatus;

            ChatMessageModel chatMessageModel = new ChatMessageModel();
            chatMessageModel.chatMessage = "Hello new Mingler";
            chatMessageModel.date = date;
            chatMessageModel.from_client_server = "client";
            chatMessageModel.ip = hostBean.ipAddress;
            chatMessageModel.name = reg.name;
            chatMessageModel.onlineStatus = hostBean.onlineStatus;
            message.OnlineStatus = hostBean.onlineStatus;
            chatMessageModel.phoneNumber = hostBean.phoneNumber;
            chatMessageModel.save();

            JSONObject jsonObject = new JSONObject();
            try {
                Gson gson = new Gson();
                jsonObject.put("message_json", gson.toJson(message));
                jsonObject.put("hostbean_json", gson.toJson(hostBean));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
                /*sendMessage(hostBean, jsonObject.toString());
                messageList.add(message);
                if (messageList != null && messagesList.getAdapter() != null) {
                    singleChatListAdapter.setUpdatedList(messageList);
                    ((SingleChatListAdapter) messagesList.getAdapter()).notifyDataSetChanged();
                }*/
        }

        private String sendDataOfUnsentMessage(Context context, HostBean hostBean, ChatMessageModel model) {
            RegistrationModel reg = RegistrationModel.first(RegistrationModel.class);
            Message message = new Message("client", model.chatMessage, getLocalIpAddress(context), model.date, reg.name, reg.phone);
            message.OnlineStatus = hostBean.onlineStatus;

            model.onlineStatus = hostBean.onlineStatus;
            //ChatMessageModel.delete(model);
            ChatMessageModel.save(model);
            //ChatMessageModel.update(model);

            JSONObject jsonObject = new JSONObject();
            try {
                Gson gson = new Gson();
                jsonObject.put("message_json", gson.toJson(message));
                jsonObject.put("hostbean_json", gson.toJson(hostBean));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        }

        public String getLocalIpAddress(Context context) {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        }
    }
}


