package com.wifimingle.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.orm.SugarContext;
import com.wifimingle.Utils.NotificationID;
import com.wifimingle.retrofit.ApiClient;

import retrofit2.Retrofit;

import static com.wifimingle.activity.ActivityMain.MY_PREFERENCES;

public class BaseApplication extends Application {

    public SharedPreferences sharedPreferences;
    public static Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        NotificationID.init();
        retrofit = ApiClient.getClient();
        SugarContext.init(this);
    }
}
