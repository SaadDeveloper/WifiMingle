/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

// Inspired by http://connectbot.googlecode.com/svn/trunk/connectbot/src/org/connectbot/bean/HostBean.java
package com.wifimingle.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;
import com.wifimingle.activity.ActivityMain;

import java.util.ArrayList;
import java.util.HashMap;

import static com.wifimingle.activity.ActivityMain.AVAILABLE;

public class HostBean extends SugarRecord implements Parcelable {

    public static final String EXTRA = ActivityMain.PKG + ".extra";
    public static final String EXTRA_POSITION = ActivityMain.PKG + ".extra_position";
    public static final String EXTRA_HOST = ActivityMain.PKG + ".extra_host";
    public static final String EXTRA_TIMEOUT = ActivityMain.PKG + ".network.extra_timeout";
    public static final String EXTRA_HOSTNAME = ActivityMain.PKG + ".extra_hostname";
    public static final String EXTRA_BANNERS = ActivityMain.PKG + ".extra_banners";
    public static final String EXTRA_PORTSO = ActivityMain.PKG + ".extra_ports_o";
    public static final String EXTRA_PORTSC = ActivityMain.PKG + ".extra_ports_c";
    public static final String EXTRA_SERVICES = ActivityMain.PKG + ".extra_services";
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
    public HashMap<Integer, String> services = null;
    public HashMap<Integer, String> banners = null;
    public ArrayList<Integer> portsOpen = null;
    public ArrayList<Integer> portsClosed = null;

    public HostBean() {
        // New object
    }

    public HostBean(HostBean bean){
        this.onlineStatus = bean.onlineStatus;
        this.ipAddress = bean.ipAddress;
        this.hostname = bean.hostname;
        this.phoneNumber = bean.phoneNumber;
        this.status = bean.status;
        this.profilePicByte = bean.profilePicByte;
        this.deviceName = bean.deviceName;
        this.hardwareAddress = bean.hardwareAddress;
        this.deviceType = bean.deviceType;
        this.isAlive = bean.isAlive;
        this.nicVendor = bean.nicVendor;
        this.os = bean.os;
    }

    public HostBean(Parcel in) {
        // Object from parcel
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(deviceType);
        dest.writeInt(isAlive);
        dest.writeString(ipAddress);
        dest.writeString(hostname);
        dest.writeString(hardwareAddress);
        dest.writeString(nicVendor);
        dest.writeString(os);
        dest.writeInt(responseTime);
        dest.writeInt(position);
        dest.writeMap(services);
        dest.writeMap(banners);
        dest.writeList(portsOpen);
        dest.writeList(portsClosed);
    }

    @SuppressWarnings("unchecked")
    private void readFromParcel(Parcel in) {
        deviceType = in.readInt();
        isAlive = in.readInt();
        ipAddress = in.readString();
        hostname = in.readString();
        hardwareAddress = in.readString();
        nicVendor = in.readString();
        os = in.readString();
        responseTime = in.readInt();
        position = in.readInt();
        services = in.readHashMap(null);
        banners = in.readHashMap(null);
        portsOpen = in.readArrayList(Integer.class.getClassLoader());
        portsClosed = in.readArrayList(Integer.class.getClassLoader());
    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public HostBean createFromParcel(Parcel in) {
            return new HostBean(in);
        }

        public HostBean[] newArray(int size) {
            return new HostBean[size];
        }
    };
}
