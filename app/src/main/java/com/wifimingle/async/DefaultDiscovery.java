/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package com.wifimingle.async;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.google.gson.Gson;
import com.wifimingle.Network.HardwareAddress;
import com.wifimingle.Network.NetInfo;
import com.wifimingle.Network.RateControl;
import com.wifimingle.Utils.Prefs;
import com.wifimingle.Utils.Save;
import com.wifimingle.activity.BaseActivity;
import com.wifimingle.interfaces.PublishHostInterface;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.NicVendorsOffline;
import com.wifimingle.thread.ChatClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.content.Context.WIFI_SERVICE;

public class DefaultDiscovery extends AbstractDiscovery implements PublishHostInterface{

    private final String TAG = "DefaultDiscovery";
    private final static int[] DPORTS = { 139, 445, 22, 80 };
    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10; //FIXME: Test, plz set in options again ?
    private final int mRateMult = 5; // Number of alive hosts between Rate
    private int pt_move = 2; // 1=backward 2=forward
//    private final int SocketServerPORT = 8080;
//    private ChatServerThread chatServerThread;
    private ExecutorService mPool;
    private boolean doRateControl;
    private RateControl mRateControl;
    private Save mSave;
    private Activity activity;

    public DefaultDiscovery(Activity discover) {
        super(discover);
        activity = discover;
        mRateControl = new RateControl();
        mSave = new Save();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //ListenForLiveHostbean(activity);
        if (mDiscover != null) {
            final BaseActivity discover = mDiscover.get();
            if (discover != null) {
                doRateControl = discover.prefs.getBoolean(Prefs.KEY_RATECTRL_ENABLE,
                        Prefs.DEFAULT_RATECTRL_ENABLE);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mDiscover != null) {
            final BaseActivity discover = mDiscover.get();
            if (discover != null) {
                Log.v(TAG, "start=" + NetInfo.getIpFromLongUnsigned(start) + " (" + start
                        + "), end=" + NetInfo.getIpFromLongUnsigned(end) + " (" + end
                        + "), length=" + size);
                mPool = Executors.newFixedThreadPool(THREADS);
                if (ip <= end && ip >= start) {
                    Log.i(TAG, "Back and forth scanning");
                    // gateway
                    launch(start);

                    // hosts
                    long pt_backward = ip;
                    long pt_forward = ip + 1;
                    long size_hosts = size - 1;

                    for (int i = 0; i < size_hosts; i++) {
                        // Set pointer if of limits
                        if (pt_backward <= start) {
                            pt_move = 2;
                        } else if (pt_forward > end) {
                            pt_move = 1;
                        }
                        // Move back and forth
                        if (pt_move == 1) {
                            launch(pt_backward);
                            pt_backward--;
                            pt_move = 2;
                        } else if (pt_move == 2) {
                            launch(pt_forward);
                            pt_forward++;
                            pt_move = 1;
                        }
                    }
                } else {
                    Log.i(TAG, "Sequencial scanning");
                    for (long i = start; i <= end; i++) {
                        launch(i);
                    }
                }
                mPool.shutdown();
                try {
                    if(!mPool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)){
                        mPool.shutdownNow();
                        Log.e(TAG, "Shutting down pool");
                        if(!mPool.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)){
                            Log.e(TAG, "Pool did not terminate");
                        }
                    }
                } catch (InterruptedException e){
                    Log.e(TAG, e.getMessage());
                    mPool.shutdownNow();
                    Thread.currentThread().interrupt();
                } finally {
                    mSave.closeDb();
                }
            }
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        if (mPool != null) {
            synchronized (mPool) {
                mPool.shutdownNow();
                // FIXME: Prevents some task to end (and close the Save DB)
            }
        }
        /*if(chatServerThread != null){
            chatServerThread.interrupt();
            Log.e("chatSever in Discovery", "chatServer interept");
        }*/
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void unused) {
        /*if(chatServerThread != null){
            chatServerThread.interrupt();
            Log.e("chatSever in Discovery", "chatServer interept");
        }*/
        super.onPostExecute(unused);
    }

    private void launch(long i) {
        if(!mPool.isShutdown()) {
            mPool.execute(new CheckRunnable(NetInfo.getIpFromLongUnsigned(i)));
        }
    }

    private int getRate() {
        if (doRateControl) {
            return mRateControl.rate;
        }

        if (mDiscover != null) {
            final BaseActivity discover = mDiscover.get();
            if (discover != null) {
                return Integer.parseInt(discover.prefs.getString(Prefs.KEY_TIMEOUT_DISCOVER,
                        Prefs.DEFAULT_TIMEOUT_DISCOVER));
            }
        }
        return 1;
    }

    private class CheckRunnable implements Runnable {
        private String addr;
        public boolean flag = false;

        CheckRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            if(isCancelled()) {
                publish(null);
            }
            Log.e(TAG, "run="+addr);
            // Create host object
            final HostBean host = new HostBean();
            host.responseTime = getRate();
            host.ipAddress = addr;
            try {
                InetAddress h = InetAddress.getByName(addr);
                // Rate control check
                if (doRateControl && mRateControl.indicator != null && hosts_done % mRateMult == 0) {
                    mRateControl.adaptRate();
                }
                // Arp Check #1
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if(!NetInfo.NOMAC.equals(host.hardwareAddress)){
                    Log.e(TAG, "found using arp #1 "+addr);
                    /*if(!sendAcknowledgeMessage(host, flag, addr)){
                        publish(host);
                    }*/
                    sendAcknowledgeMessage(host);
                    publish(host);
                    return;
                }

                // Native InetAddress check
                if (h.isReachable(getRate())) {
                    Log.e(TAG, "found using InetAddress ping "+addr);
                    publish(host);
                    sendAcknowledgeMessage(host);
                    /*if(!sendAcknowledgeMessage(host, flag, addr)){
                        publish(host);
                    }*/
                    // Set indicator and get a rate
                    if (doRateControl && mRateControl.indicator == null) {
                        mRateControl.indicator = addr;
                        mRateControl.adaptRate();
                    }
                    return;
                }

                // Arp Check #2
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if(!NetInfo.NOMAC.equals(host.hardwareAddress)){
                    Log.e(TAG, "found using arp #2 "+addr);
                    /*if(!sendAcknowledgeMessage(host, flag, addr)){
                        publish(host);
                    }*/
                    publish(host);
                    sendAcknowledgeMessage(host);
                    return;
                }
                // Custom check
                int port;
                // TODO: Get ports from options
                Socket s = new Socket();
                for (int i = 0; i < DPORTS.length; i++) {
                    try {
                        s.bind(null);
                        s.connect(new InetSocketAddress(addr, DPORTS[i]), getRate());
                        Log.v(TAG, "found using TCP connect "+addr+" on port=" + DPORTS[i]);
                    } catch (IOException e) {
                    } catch (IllegalArgumentException e) {
                    } finally {
                        try {
                            s.close();
                        } catch (Exception e){
                        }
                    }
                }

                // Arp Check #3
                host.hardwareAddress = HardwareAddress.getHardwareAddress(addr);
                if(!NetInfo.NOMAC.equals(host.hardwareAddress)){
                    Log.e(TAG, "found using arp #3 "+addr);
                    /*if(!sendAcknowledgeMessage(host, flag, addr)){
                        publish(host);
                    }*/
                    publish(host);
                    sendAcknowledgeMessage(host);
                    return;
                }
                publish(null);

            } catch (IOException e) {
                publish(null);
                Log.e(TAG, e.getMessage());
            } 
        }
    }

    /*private boolean sendAcknowledgeMessage(HostBean host, boolean flag, String addr){
        long startTime = System.currentTimeMillis();
        long currentTime =startTime;
        ChatClient chatClient = new ChatClient(addr.trim());
        chatClient.start();
        chatClient.sendMsg("$h");
        Log.e("ClientServer1", String.valueOf(currentTime) + " , " + addr.trim());
        while(currentTime < startTime + 5000){
            //Do something here
            if(chatClient.getMsgLog() != null && chatClient.getMsgLog().substring(0, 2).equals("$s")){
                //List<String> fullMessage = Arrays.asList(chatClient.getMsgLog().split(","));
                String msg = chatClient.getMsgLog();
                String status = msg.substring(2, msg.indexOf(","));
                String registrationString = msg.substring(msg.indexOf(",") + 1);
                //RegistrationModel reg = new Gson().fromJson(chatClient.getMsgLog().substring(2), RegistrationModel.class);
                RegistrationModel reg = new Gson().fromJson(registrationString, RegistrationModel.class);
                reg.status = status;
                Log.e("ClientServer", "get from server response of severHello , " + reg.name);
                Log.e("ClientServer", String.valueOf(currentTime) + " , " + addr.trim());
                host.onlineStatus = true;
                host.deviceName = reg.name;
                host.status = reg.status;
                host.phoneNumber = reg.phone;
                host.profilePicByte = reg.profilePic;
                flag = true;
                publish(host);
                chatClient.disconnect();
                break;
            }
            currentTime =System.currentTimeMillis();
        }
        return flag;
    }*/

    private String getDeviceVendorName(String macAdress) {
        String dataUrl = "http://api.macvendors.com/" + macAdress;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(dataUrl);
            connection = (HttpURLConnection) url.openConnection();
            /*connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoInput(true);
            connection.setDoOutput(true);*/
            /*DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.flush();
            wr.close();*/
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String responseStr = response.toString();
            Log.e("Server response", responseStr);
            return responseStr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void sendAcknowledgeMessage(HostBean host){
        String hostString = new Gson().toJson(host);

        if(!host.ipAddress.equals(getLocalIpAddress())){
            final ChatClient chatClient = new ChatClient(host.ipAddress);
            chatClient.start();
            chatClient.sendMsg("$h" + getLocalIpAddress() + "," + hostString);
            chatClient.interrupt();
        }
        /*chatClient.disconnect();
        chatClient.interrupt();*/
        /*if(!host.ipAddress.equals(getLocalIpAddress())) {
            startSocket(host.ipAddress, 53705, "$h" + getLocalIpAddress() + "," + hostString);
        }*/
        /*new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chatClient.disconnect();
            }
        }, 500);*/
    }

    private void startSocket(String dstAddress, int SocketServerPORT, String msgToSend){
        Socket socket = null;
        DataOutputStream dataOutputStream = null;
        //DataInputStream dataInputStream = null;

        try {
            socket = new Socket(dstAddress, SocketServerPORT);
            //dataOutputStream = new DataOutputStream(socket.getOutputStream());
            //dataInputStream = new DataInputStream(socket.getInputStream());

            if (!msgToSend.equals("")) {
                //dataOutputStream.writeUTF(msgToSend);
                //dataOutputStream.flush();
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
                    Log.e("socket closing", "Socket Closed");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                    Log.e("dataOutputStreamClosing", "dataOutputStream Closed");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            /*if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                    Log.e("dataInputStreamClosing", "dataInputStream Closed");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }*/
        }
    }

    public String getLocalIpAddress() {
        WifiManager wm = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    /*private void ListenForLiveHostbean(Context context){
        chatServerThread = new ChatServerThread(context);
        chatServerThread.start();
    }*/

    @Override
    public void publishHost(HostBean hostBean) {
        publish(hostBean);
    }

    private void publish(final HostBean host) {
        hosts_done++;
        if(host == null){
            publishProgress((HostBean) null);
            return; 
        }

        if (mDiscover != null) {
            final BaseActivity discover = mDiscover.get();
            if (discover != null) {
                // Mac Addr not already detected
                if(NetInfo.NOMAC.equals(host.hardwareAddress)){
                    host.hardwareAddress = HardwareAddress.getHardwareAddress(host.ipAddress);
                }

                // NIC vendor
                //host.nicVendor = HardwareAddress.getNicVendor(host.hardwareAddress);

                // Is gateway ?
                if (discover.net.gatewayIp.equals(host.ipAddress)) {
                    host.deviceType = HostBean.TYPE_GATEWAY;
                }

                // FQDN
                // Static
                /*if ((host.hostname = mSave.getCustomName(host)) == null) {
                    // DNS
                    if (discover.prefs.getBoolean(Prefs.KEY_RESOLVE_NAME,
                            Prefs.DEFAULT_RESOLVE_NAME) == true) {
                        try {
                            host.hostname = (InetAddress.getByName(host.ipAddress)).getCanonicalHostName();
                        } catch (UnknownHostException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    // TODO: NETBIOS
                    //try {
                    //    host.hostname = NbtAddress.getByName(addr).getHostName();
                    //} catch (UnknownHostException e) {
                    //    Log.i(TAG, e.getMessage());
                    //}
                }*/
            }
        }
        //String [] selectionArgs = {host.hardwareAddress.substring(0, 8) + "%"};
        //ArrayList<NicVendorsOffline> list = (ArrayList<NicVendorsOffline>) NicVendorsOffline.findWithQuery(NicVendorsOffline.class, "select mac_Address from NIC_VENDORS_OFFLINE where mac_Address=?", selectionArgs);
        ArrayList<NicVendorsOffline> list = (ArrayList<NicVendorsOffline>) NicVendorsOffline.find(NicVendorsOffline.class, "mac_Address = ?", host.hardwareAddress.substring(0, 8).toUpperCase());
        //host.hostname = getDeviceVendorName(host.hardwareAddress);
        if(list.size() > 0){
            if(list.get(0).companyFullName != null){
                host.hostname = list.get(0).companyFullName;
            }else {
                host.hostname = list.get(0).companyName;
            }
        }
        publishProgress(host);
    }

    /*private class ChatServerThread extends Thread {

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

        ConnectThread(Socket socket, Context context) {
            this.socket = socket;
            this.context = context;
            serverName = "";
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    if (dataInputStream.available() > 0) {
                        String newMsg = dataInputStream.readUTF();
                        if(newMsg.substring(0, 2).equals("$s")){
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
                            publish(host);
                        }else {
                            break;
                        }
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
    }*/
}
