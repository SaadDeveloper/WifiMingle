package com.wifimingle.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.wifimingle.Network.NetInfo;
import com.wifimingle.Utils.Prefs;

/**
 * Created by BrOlLy on 30/11/2017.
 */

public class BaseActivity extends ActivityNet {

    public final static String TAG = "BaseActivity";
    public static final String PKG = "com.wifimingle";
    private int currentNetwork = 0;
    public long network_ip = 0;
    public long network_start = 0;
    public long network_end = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void setInfo() {
        if (currentNetwork != net.hashCode()) {
            Log.i(TAG, "Network info has changed");
            currentNetwork = net.hashCode();

            // Cancel running tasks
            //cancelTasks();
        } else {
            return;
        }

        // Get ip information
        network_ip = NetInfo.getUnsignedLongFromIp(net.ip);
        if (prefs.getBoolean(Prefs.KEY_IP_CUSTOM, Prefs.DEFAULT_IP_CUSTOM)) {
            // Custom IP
            network_start = NetInfo.getUnsignedLongFromIp(prefs.getString(Prefs.KEY_IP_START,
                    Prefs.DEFAULT_IP_START));
            network_end = NetInfo.getUnsignedLongFromIp(prefs.getString(Prefs.KEY_IP_END,
                    Prefs.DEFAULT_IP_END));
        } else {
            // Custom CIDR
            if (prefs.getBoolean(Prefs.KEY_CIDR_CUSTOM, Prefs.DEFAULT_CIDR_CUSTOM)) {
                net.cidr = Integer.parseInt(prefs.getString(Prefs.KEY_CIDR, Prefs.DEFAULT_CIDR));
            }
            // Detected IP
            int shift = (32 - net.cidr);
            if (net.cidr < 31) {
                network_start = (network_ip >> shift << shift) + 1;
                network_end = (network_start | ((1 << shift) - 1)) - 1;
            } else {
                network_start = (network_ip >> shift << shift);
                network_end = (network_start | ((1 << shift) - 1));
            }
            // Reset ip start-end (is it really convenient ?)
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(Prefs.KEY_IP_START, NetInfo.getIpFromLongUnsigned(network_start));
            edit.putString(Prefs.KEY_IP_END, NetInfo.getIpFromLongUnsigned(network_end));
            edit.commit();
        }
    }

    @Override
    protected void setButtons(boolean disable) {

    }

    @Override
    protected void cancelTasks() {

    }

    @Override
    protected void startTasks() {

    }

    public void makeToast(int msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
