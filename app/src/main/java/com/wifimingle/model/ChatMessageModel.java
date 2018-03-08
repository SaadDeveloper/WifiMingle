package com.wifimingle.model;

import com.orm.SugarRecord;

import static com.wifimingle.activity.ActivitySingleChat.SEEN;

/**
 * Created by BrOlLy on 13/12/2017.
 */

public class ChatMessageModel extends SugarRecord {

    public String name;
    public String chatMessage;
    public String date;
    public String from_client_server;
    public String phoneNumber;
    public String ip;
    public String seen_unseen = SEEN;
    public boolean onlineStatus;
    public String imagePath;
    public int notificationId;

    public ChatMessageModel() {
    }
}
