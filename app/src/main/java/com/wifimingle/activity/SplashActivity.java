package com.wifimingle.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.wifimingle.R;
import com.wifimingle.async.ReadTextFile;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.NicVendorsOffline;
import com.wifimingle.service.ListenForImageRecieving;
import com.wifimingle.service.ListeningForOnlineStatus;
import com.wifimingle.service.ServicForClosingAppScenario;

import java.util.ArrayList;

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

        startService(new Intent(SplashActivity.this, ServicForClosingAppScenario.class));
        initActivity();
    }

    private void initActivity() {
        progressBar = findViewById(R.id.loader);
        progressText = findViewById(R.id.progress_text);
        hostBeansMinglers = new ArrayList<>();
        hostBeansOthers = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void fetchData() {
        ArrayList<HostBean> allHostBeans = (ArrayList<HostBean>) HostBean.listAll(HostBean.class);
        if (allHostBeans.size() > 0) {
            hostBeansMinglers.addAll(allHostBeans);
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
