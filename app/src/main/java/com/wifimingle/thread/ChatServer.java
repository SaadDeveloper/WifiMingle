package com.wifimingle.thread;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.google.gson.Gson;
import com.wifimingle.async.DefaultDiscovery;
import com.wifimingle.constants.Constants;
import com.wifimingle.interfaces.PublishHostInterface;
import com.wifimingle.model.ChatMessageModel;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.Message;
import com.wifimingle.model.PhoneModelForWelcomingMessage;
import com.wifimingle.model.RegistrationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
                    /*if (connectThread != null) {
                        connectThread.setDataInputStream(socket);
                        Log.e("insideIf", "setDataInputStream method calls");
                    } else {
                        Log.e("insideIf", "connect thread initializes");
                        connectThread = new ConnectThread(socket, activity);
                        connectThread.start();
                    }*/
                    connectThread = new ConnectThread(socket, activity);
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

    /*public void sendMsgServer(String s) {
        if (connectThread != null) {
            connectThread.sendMsg(s);
        }
    }*/

    private class ConnectThread extends Thread {

        Socket socket;
        Activity activity;
        String msgToSend = "";
        String serverName;
        /*DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;*/
        ObjectInputStream dataInputStream = null;

        ConnectThread(Socket socket, Activity activity) {
            this.socket = socket;
            this.activity = activity;
            serverName = "";
        }

        private void setDataInputStream(Socket dataInputStreamSocket) {
            try {
                ///dataInputStream = new DataInputStream(dataInputStreamSocket.getInputStream());
                //dataOutputStream = new DataOutputStream(dataInputStreamSocket.getOutputStream());
                dataInputStream = new ObjectInputStream(dataInputStreamSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

                /*dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());*/
            String newMsg = recieveMessageMechanismForIphone(socket);
            newMsg = newMsg.replace("~", "").replace("~", "").replace("~", "")
                    .replace("~", "").replace("~", "");
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
                host.profilePicString = reg.profilePic;
                publishHostInterface.publishHost(host);

                long count = PhoneModelForWelcomingMessage.count(PhoneModelForWelcomingMessage.class);
                if (count > 0) {
                    List<PhoneModelForWelcomingMessage> found = PhoneModelForWelcomingMessage.find(PhoneModelForWelcomingMessage.class, "phone = ?", reg.phone);
                    if (found.size() == 0) {
                        PhoneModelForWelcomingMessage phone = new PhoneModelForWelcomingMessage();
                        phone.setPhone(reg.phone);
                        phone.save();
                        String jsonString = insertData(activity, host);
                        ChatClient chatClient = new ChatClient(host.ipAddress);
                        chatClient.start();
                        chatClient.sendMsg("$Welcome" + "," + jsonString + Constants.HEADER);
                        chatClient.interrupt();
                    }
                } else {
                    PhoneModelForWelcomingMessage phone = new PhoneModelForWelcomingMessage();
                    phone.setPhone(reg.phone);
                    phone.save();
                    String jsonString = insertData(activity, host);
                    ChatClient chatClient = new ChatClient(host.ipAddress);
                    chatClient.start();
                    chatClient.sendMsg("$Welcome" + "," + jsonString + Constants.HEADER);
                    chatClient.interrupt();
                }

                ArrayList<ChatMessageModel> forUnsentMesageList = (ArrayList<ChatMessageModel>) ChatMessageModel.find(ChatMessageModel.class, "phone_Number = ?", reg.phone);
                if (forUnsentMesageList.size() > 0) {
                    ChatClient chatClient = new ChatClient(host.ipAddress);
                    chatClient.start();
                    for (ChatMessageModel m : forUnsentMesageList) {
                        if (!m.onlineStatus) {
                            Log.e("Error_Hello", "Error Hello message");
                            chatClient.sendMsg(sendDataOfUnsentMessage(activity, host, m) + Constants.HEADER);
                        }
                    }
                    chatClient.interrupt();
                }
            }
                /*while (true) {
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
                            host.profilePicString = reg.profilePic;
                            publishHostInterface.publishHost(host);

                            long count = PhoneModelForWelcomingMessage.count(PhoneModelForWelcomingMessage.class);
                            if (count > 0) {
                                List<PhoneModelForWelcomingMessage> found = PhoneModelForWelcomingMessage.find(PhoneModelForWelcomingMessage.class, "phone = ?", reg.phone);
                                if (found.size() == 0) {
                                    PhoneModelForWelcomingMessage phone = new PhoneModelForWelcomingMessage();
                                    phone.setPhone(reg.phone);
                                    phone.save();
                                    String jsonString = insertData(activity, host);
                                    ChatClient chatClient = new ChatClient(host.ipAddress);
                                    chatClient.start();
                                    chatClient.sendMsg("$Welcome" + "," + jsonString);
                                    chatClient.interrupt();
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
                            }

                            ArrayList<ChatMessageModel> forUnsentMesageList = (ArrayList<ChatMessageModel>) ChatMessageModel.find(ChatMessageModel.class, "phone_Number = ?", reg.phone);
                            if (forUnsentMesageList.size() > 0) {
                                ChatClient chatClient = new ChatClient(host.ipAddress);
                                chatClient.start();
                                for (ChatMessageModel m : forUnsentMesageList) {
                                    if (!m.onlineStatus) {
                                        Log.e("Error_Hello", "Error Hello message");
                                        chatClient.sendMsg(sendDataOfUnsentMessage(activity, host, m));
                                    }
                                }
                                chatClient.interrupt();
                            }
                        } else {
                            Log.e("insideIf", "breaking while loop");
                            break;
                        }
                    }
                }*/
        }
    }


        /*private void sendMsg(String msg) {
            msgToSend = msg;
        }*/

    private String recieveMessageMechanismForIphone(Socket socket){
        //Socket socket = null;
        DataInputStream dataInputStream = null;
        try {
            byte[] mmBuffer = new byte[8192];
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

    private String insertData(Context context, HostBean hostBean) {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_TIME);
        Date dateCurrent = new Date();

        String date = sdf.format(dateCurrent);

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


