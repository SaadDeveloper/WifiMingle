package com.wifimingle.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wifimingle.activity.TabActivity;

/**
 * Created by BrOlLy on 29/06/2017.
 */

public class ServicForClosingAppScenario extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if(TabActivity.chatServer != null){
            TabActivity.chatServer.interrupt();
            Log.e("Closing Service", "chat server intereupt");
        }else {
            Log.e("Closing Service", "chat server is null");
        }
        // Destroy the service
        stopSelf();
    }
}
