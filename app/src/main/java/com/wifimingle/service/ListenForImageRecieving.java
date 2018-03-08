package com.wifimingle.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wifimingle.thread.ChatServerForImage;

import static com.wifimingle.activity.ActivityMain.TAG;

/**
 * Created by Saad on 3/2/2018.
 */

public class ListenForImageRecieving extends Service {

    private ChatServerForImage chatServerForImage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Listen Image Service onDestroy called");
        if (chatServerForImage != null) {
            chatServerForImage.interrupt();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        chatServerForImage = new ChatServerForImage(this);
        chatServerForImage.start();
        Log.e(TAG, "Listen Image Service OnStartCommand called");
        return START_STICKY;
    }
}
