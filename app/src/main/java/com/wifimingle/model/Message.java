package com.wifimingle.model;

import java.io.Serializable;

public class Message implements Serializable{

    public String client_server;
    public String message;
    public String ip;
    public String time;
    public String name;
    public String phone;
    public boolean OnlineStatus;
    public String message_seen;
    public byte[] imagebyte;

    public Message() {
    }

    public Message(String client_server, String message, String ip, String time, String name, String phone) {
        this.client_server = client_server;
        this.message = message;
        this.ip = ip;
        this.time = time;
        this.name = name;
        this.phone = phone;
    }
}
