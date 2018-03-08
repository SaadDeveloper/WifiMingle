package com.wifimingle.model;

import com.orm.SugarRecord;

import static com.wifimingle.activity.ActivityMain.AVAILABLE;

/**
 * Created by BrOlLy on 02/01/2018.
 */

public class HostBeansList extends SugarRecord {

    public static final int TYPE_GATEWAY = 0;
    public static final int TYPE_COMPUTER = 1;

    public boolean onlineStatus = false;
    public String status = AVAILABLE;
    public String deviceName = null;
    //public String profilePicString;
    public byte[] profilePicByte = null;
    public String phoneNumber;

    public int deviceType = TYPE_COMPUTER;
    public int isAlive = 1;
    public int position = 0;
    public int responseTime = 0; // ms
    public String ipAddress = null;
    public String hostname = null;
    public String hardwareAddress = com.wifimingle.Network.NetInfo.NOMAC;
    public String nicVendor = "Unknown";
    public String os = "Unknown";


}
