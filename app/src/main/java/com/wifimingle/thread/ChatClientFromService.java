package com.wifimingle.thread;

import android.os.Build;
import android.text.TextUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class ChatClientFromService extends Thread {
    private final int SocketServerPORT = 8080;
    //private final int SocketServerPORT = 53705;
    private String dstAddress;

    private String msgToSend = "";
    private boolean goOut = false;
    private String msgLog;
    private byte[] msgLogByte;

    private Socket socket = null;
    private DataOutputStream dataOutputStream = null;
    /*private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;*/
    //private ObjectOutputStream dataOutputStream = null;

    public ChatClientFromService(String address) {
        dstAddress = address;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

    @Override
    public void run() {

        try {
            socket = new Socket(dstAddress, SocketServerPORT);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            /*dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());*/
            //dataOutputStream = new ObjectOutputStream(socket.getOutputStream());

            /*if (!msgToSend.equals("")) {
                dataOutputStream.writeUTF(msgToSend);
                dataOutputStream.flush();
            }*/
            if (!msgToSend.equals("")) {
                dataOutputStream.write(msgToSend.getBytes());
                //dataOutputStream.writeUTF(msgToSend);
                dataOutputStream.flush();
            }

            /*while (dataInputStream.available() < 0)
                ; //Server will send its name the very first time, client waits till then
            setMsgLog(dataInputStream.readUTF());*/
            //setMsgLogByte(convertInputStreamToByteArray(dataInputStream));


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

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
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
        //disconnect();
    }

    public byte[] convertInputStreamToByteArray(DataInputStream in) {
        try {
            byte[] buffer = new byte[10];
            int read = 0;

            while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                in.read(buffer);
                System.out.println("Server says " + Arrays.toString(buffer));
            }
            return buffer;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void sendMsg(String msg) {
        msgToSend = msg;
    }

    public String getMsgLog() {
        return msgLog;
    }

    public byte[] getMsgLogByte() {
        return msgLogByte;
    }

    public void setMsgLogByte(byte[] msgLog) {
        msgLogByte = msgLog;
    }

    public void setMsgLog(String msgLog) {
        this.msgLog = msgLog;
    }

    public void disconnect() {
//        goOut = true;
        if (socket != null) {
            try {
                socket.close();
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
    }
}
