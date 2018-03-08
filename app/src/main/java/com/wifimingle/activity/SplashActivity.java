package com.wifimingle.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.wifimingle.R;
import com.wifimingle.async.ReadTextFile;
import com.wifimingle.model.FeedBackModel;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.NicVendorsOffline;
import com.wifimingle.model.PhoneModelForWelcomingMessage;
import com.wifimingle.service.ListenForImageRecieving;
import com.wifimingle.service.ListeningForOnlineStatus;
import com.wifimingle.service.ServicForClosingAppScenario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity{

    private ReadTextFile readTextFile;
    private Context ctxt;
    private ProgressBar progressBar;
    private TextView progressText;
    private ArrayList<HostBean> hostBeansMinglers;
    private ArrayList<HostBean> hostBeansOthers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        ctxt = this;
        if (!isMyServiceRunning(ListeningForOnlineStatus.class)) {
            startService(new Intent(SplashActivity.this, ListeningForOnlineStatus.class));
        }
        if(!isMyServiceRunning(ListenForImageRecieving.class)){
            startService(new Intent(SplashActivity.this, ListenForImageRecieving.class));
        }
        //startService(new Intent(SplashActivity.this, ListeningForOnlineStatus.class));
        startService(new Intent(SplashActivity.this, ServicForClosingAppScenario.class));
        initActivity();
        //initDiscovery();
    }

    private void initActivity() {
        progressBar = findViewById(R.id.loader);
        progressText = findViewById(R.id.progress_text);
        hostBeansMinglers = new ArrayList<>();
        hostBeansOthers = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);

        /*prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Prefs.KEY_INTF, Prefs.DEFAULT_INTF);
        //phase3(ctxt);
        startService(new Intent(SplashActivity.this, ListeningForOnlineStatus.class));*/
    }

    private void fetchData() {
        //HostBean.deleteAll(HostBean.class);
        ArrayList<HostBean> allHostBeans = (ArrayList<HostBean>) HostBean.listAll(HostBean.class);
        if (allHostBeans.size() > 0) {
            hostBeansMinglers.addAll(allHostBeans);
            //startDiscoverActivity();
        }
        if (NicVendorsOffline.count(NicVendorsOffline.class) <= 0) {
            populateDbForOfflineNicVendors();
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("MAC-Addresses Info Loading .... Please wait");
        } else {

            startDiscoverActivity();

        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getCanonicalName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void populateDbForOfflineNicVendors() {
        readTextFile = new ReadTextFile(this);
        readTextFile.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void startDiscoverActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                Intent intent = new Intent(ctxt, TabActivity.class);
                TabActivity.hostBeansForMinglers = hostBeansMinglers;
                TabActivity.hostBeansForOthers = hostBeansOthers;
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
