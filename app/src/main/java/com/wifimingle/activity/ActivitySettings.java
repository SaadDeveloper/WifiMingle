package com.wifimingle.activity;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wifimingle.R;
import com.wifimingle.constants.Constants;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.RegistrationModel;
import com.wifimingle.thread.ChatClient;

import static com.wifimingle.activity.ActivityMain.AVAILABLE;
import static com.wifimingle.activity.ActivityMain.BUSY;
import static com.wifimingle.activity.ActivityMain.MY_PREFERENCES;

public class ActivitySettings extends AppCompatActivity {

    private RadioButton radioAvail;
    private RadioButton radioBusy;
    private View viewOnline;
    private View viewOffline;
    private SeekBar seekBar_timer;
    private TextView tvCurrentTimeSet;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ActionBar actionBar;
    private Drawable drawable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.dark_gray));
        }
        setContentView(R.layout.activity_settings);

        init();
        setMenuHomeButton();
        populateData();
        initListeners();
    }

    private void init(){
        radioAvail = findViewById(R.id.radio_avail);
        radioBusy = findViewById(R.id.radio_busy);
        seekBar_timer = findViewById(R.id.seekbar_timer);
        tvCurrentTimeSet = findViewById(R.id.current_time_set);
        viewOnline = findViewById(R.id.view_online);
        viewOffline = findViewById(R.id.view_offline);
        actionBar = getSupportActionBar();
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private void setMenuHomeButton(){
        drawable = getResources().getDrawable(R.drawable.chat_header);
        actionBar.setBackgroundDrawable(drawable);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    private void populateData(){
        RegistrationModel registrationModel = RegistrationModel.first(RegistrationModel.class);

        if(registrationModel.status.equals(AVAILABLE)){
            radioAvail.setChecked(true);
            viewOnline.setBackgroundColor(getResources().getColor(R.color.orange));
            viewOffline.setBackgroundColor(getResources().getColor(R.color.grey));
        }else {
            radioBusy.setChecked(true);
            viewOffline.setBackgroundColor(getResources().getColor(R.color.orange));
            viewOnline.setBackgroundColor(getResources().getColor(R.color.grey));
        }

        //seekBar_timer.setMin(5);
        seekBar_timer.setMax(11);
        int timer = sharedPreferences.getInt("timerForScan", 5000);
        if(timer == 5000) {
            seekBar_timer.setProgress(0);
            tvCurrentTimeSet.setText("Current Time set is: " + 5 + " sec");
        }else {
            seekBar_timer.setProgress(((timer / 1000) / 5) - 1);
            tvCurrentTimeSet.setText("Current Time set is: " + timer / 1000 + " sec");
        }
    }

    private void initListeners(){
        radioAvail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    RegistrationModel registrationModel = RegistrationModel.first(RegistrationModel.class);
                    viewOnline.setBackgroundColor(getResources().getColor(R.color.orange));
                    viewOffline.setBackgroundColor(getResources().getColor(R.color.grey));
                    registrationModel.status = AVAILABLE;
                    registrationModel.save();
                    sendAvailabilityStatus(AVAILABLE);
                }
            }
        });

        radioBusy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    RegistrationModel registrationModel = RegistrationModel.first(RegistrationModel.class);
                    viewOffline.setBackgroundColor(getResources().getColor(R.color.orange));
                    viewOnline.setBackgroundColor(getResources().getColor(R.color.grey));
                    registrationModel.status = BUSY;
                    registrationModel.save();
                    sendAvailabilityStatus(BUSY);
                }
            }
        });

        seekBar_timer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = (seekBar.getProgress() * 5) + 5;
                editor.putInt("timerForScan", progress * 1000);
                editor.apply();
                //SplashActivity.timerForRescan = progress * 1000;
                String str = String.valueOf(progress);
                tvCurrentTimeSet.setText("Current Time set is: " + str + " sec");
                Toast.makeText(ActivitySettings.this, str + " Seconds", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAvailabilityStatus(String status){
        if(TabActivity.hostBeansForMinglers != null && TabActivity.hostBeansForMinglers.size() > 0){
            for(HostBean hostBean: TabActivity.hostBeansForMinglers){
                //String hostBeanString = new Gson().toJson(hostBean);
                ChatClient chatClient = new ChatClient(hostBean.ipAddress);
                chatClient.start();
                chatClient.sendMsg("$status"+ ";" + status + "," + getLocalIpAddress() + Constants.HEADER);
                chatClient.interrupt();
            }
        }
    }

    public String getLocalIpAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
