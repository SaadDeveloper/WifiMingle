package com.wifimingle.Utils;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationID {

    private static AtomicInteger c;

    public static void init(){
        c = new AtomicInteger();
    }
    public static int getID() {
        return c.incrementAndGet();
    }

    /*public static void initialize(int a){
        c = new AtomicInteger(a);
    }
    public static int getID(Context context) {
        if(c == null){
            SharedPreferences sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
            int a = sharedPreferences.getInt(GET_NOTIFICATION_ID, 0);
            c = new AtomicInteger(a);
            return c.incrementAndGet();
        }
        return c.incrementAndGet();
    }*/
}