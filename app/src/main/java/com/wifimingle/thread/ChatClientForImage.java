package com.wifimingle.thread;

import android.os.Build;
import android.text.TextUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class ChatClientForImage extends Thread {
    private final int SocketServerPORT = 8000;
    //private final int SocketServerPORT = 53705;
    private String dstAddress;

    private byte[] imageByte;
    private boolean goOut = false;
    private String msgLog;
    private byte[] msgLogByte = null;

    public ChatClientForImage(String address) {
        dstAddress = address;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(dstAddress, SocketServerPORT);

            //ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            if(imageByte != null) {
                dataOutputStream.write(imageByte);
                dataOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
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

    public void sendImage(byte[] msg) {
        imageByte = msg;
    }
}
