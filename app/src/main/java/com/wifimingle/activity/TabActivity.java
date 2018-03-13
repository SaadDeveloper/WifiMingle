package com.wifimingle.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.wifimingle.R;
import com.wifimingle.adapter.ViewPagerAdapter;
import com.wifimingle.async.AbstractDiscovery;
import com.wifimingle.async.DefaultDiscovery;
import com.wifimingle.fragments.MinglersTabFragment;
import com.wifimingle.fragments.OthersTabFragment;
import com.wifimingle.interfaces.FragmentMethodCallingMinglers;
import com.wifimingle.interfaces.FragmentMethodCallingOthers;
import com.wifimingle.model.HostBean;
import com.wifimingle.thread.ChatServer;

import java.util.ArrayList;

import static com.wifimingle.activity.ActivityMain.ALL;
import static com.wifimingle.activity.ActivityMain.AVAILABLE;
import static com.wifimingle.activity.ActivityMain.BUSY;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST_OTHERS;
import static com.wifimingle.activity.ActivityMain.MY_PREFERENCES;
import static com.wifimingle.activity.ActivityMain.NO_MINGLER;
import static com.wifimingle.activity.ActivityMain.OFFLINE;
import static com.wifimingle.activity.ActivityMain.SCREEN_MESSAGE;
import static com.wifimingle.activity.ActivityMain.WIFI_DC;

public class TabActivity extends BaseActivity {

    public static ChatServer chatServer;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView ivFeedback;
    private ViewPagerAdapter viewPagerAdapter;
    private AbstractDiscovery mDiscoveryTask;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static String FILTER = null;
    public static boolean isScanStart = false;
    public static ArrayList<HostBean> hostBeansForMinglers;
    public static ArrayList<HostBean> hostBeansForOthers;
    private FragmentMethodCallingMinglers callingInterfaceMinglers;
    private FragmentMethodCallingOthers callingInterfaceOthers;
    private MinglersTabFragment minglerFragment;
    private OthersTabFragment otherFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.dark_gray));
        }
        setContentView(R.layout.activity_tab);

        init();
        initListeners();
        setupTabLayoutAndViewPager();
    }

    private void init(){
        customTabLayout();
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        ivFeedback = findViewById(R.id.iv_feedback);
        chatServer = new ChatServer(TabActivity.this);
        chatServer.start();
        if(hostBeansForMinglers == null) {
            ArrayList<HostBean> allHostBeans = (ArrayList<HostBean>) HostBean.listAll(HostBean.class);
            if (allHostBeans.size() > 0) {
                hostBeansForMinglers.addAll(allHostBeans);

            }
        }
    }

    private void initListeners(){
        ivFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabActivity.this, FeedBackActivity.class);
                startActivity(intent);
            }
        });

        ivFeedback.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast toast = Toast.makeText(TabActivity.this, "Contact Us", Toast.LENGTH_SHORT);
                int width = getResources().getDimensionPixelSize(R.dimen._10sdp);
                int height = getResources().getDimensionPixelSize(R.dimen._60sdp);
                toast.setGravity(Gravity.TOP|Gravity.LEFT, width, height);
                toast.show();
                return true;
            }
        });
    }

    public void setFetchedMinglersProgress(String minglersProgress){
        minglerFragment.settingMinglerProgress(minglersProgress);
    }

    public void customTabLayout(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_tab_layout);
    }

    private void setupTabLayoutAndViewPager() {
        minglerFragment = (MinglersTabFragment) MinglersTabFragment.newInstance(hostBeansForMinglers);
        otherFragment = (OthersTabFragment) OthersTabFragment.newInstance(hostBeansForOthers);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(minglerFragment, "MINGLERS");
        viewPagerAdapter.addFragment(otherFragment, "OTHERS");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this,R.color.orange));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setListners1(minglerFragment);
        setListners2(otherFragment);
    }

    private void initDiscovery(){
        mDiscoveryTask = new DefaultDiscovery(TabActivity.this);
        mDiscoveryTask.setNetwork(network_ip, network_start, network_end);
        mDiscoveryTask.execute();
        chatServer.setNewAsyncDiscoveryTask((DefaultDiscovery) mDiscoveryTask);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        chatServer.interrupt();
        cancelTasks();
        finish();
    }

    @Override
    public void cancelTasks() {
        if(mDiscoveryTask != null) {
            mDiscoveryTask.cancel(true);
        }
        isScanStart = false;
        setFetchedMinglersProgress(null);
        ArrayList<HostBean> listt = (ArrayList<HostBean>) HostBean.listAll(HostBean.class);
        if(listt.size() > 0){
            for (HostBean h: listt){
                h.status = OFFLINE;
                h.onlineStatus = false;
                HostBean.save(h);
            }
        }

        Intent intent = new Intent(INTENT_FILTER_BROADCAST);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("host_list", listt);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(ctxt).sendBroadcast(intent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ActivityMain.SCREEN_MESSAGE = WIFI_DC;
                setIndicationMessage();
            }
        }, 200);
    }

    @Override
    protected void startTasks() {
        if(!isScanStart) {
            if (hostBeansForMinglers.size() == 0) {
                ActivityMain.SCREEN_MESSAGE = NO_MINGLER;
            } else {
                SCREEN_MESSAGE = "";
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initDiscovery();
                    setIndicationMessage();
                }
            }, 1000);
        }
    }

    public void setIndicationMessage(){
        if(callingInterfaceMinglers != null && callingInterfaceOthers != null){
            callingInterfaceMinglers.updateListMinglers();
            callingInterfaceOthers.updateListOthers();
        }
    }

    public void setListners1(FragmentMethodCallingMinglers callingInterface){
        this.callingInterfaceMinglers = callingInterface;
    }

    public void setListners2(FragmentMethodCallingOthers callingInterface){
        this.callingInterfaceOthers = callingInterface;
    }

    public void setHostBeanListMinglers(ArrayList<HostBean> list){
        if(checkWifiStatus()) {

            list = checkDuplicate(list);

            boolean forFirstTime = false;
            ArrayList<HostBean> hostBeans = (ArrayList<HostBean>) HostBean.listAll(HostBean.class);
            ArrayList<HostBean> copyListForLoop = new ArrayList<>(hostBeans);
            if(copyListForLoop.size() == 0 && list.size() > 0){
                HostBean.saveInTx(list);
                forFirstTime = true;
            }else {
                if (list.size() > 0){
                    for (HostBean scanned: list) {
                        boolean newDevice = true;
                        int index = 0;
                        for (HostBean saved : copyListForLoop) {
                            if (saved.ipAddress.equals(scanned.ipAddress)) {
                                hostBeans.remove(index);
                                hostBeans.add(index, scanned);
                                newDevice = false;
                                break;
                            }
                            index = index + 1;
                        }
                        if (newDevice) {
                            hostBeans.add(scanned);
                        }
                    }
                }
            }

            if(!forFirstTime){
                HostBean.deleteAll(HostBean.class);
                HostBean.saveInTx(hostBeans);
            }

            ArrayList<HostBean> list1 = (ArrayList<HostBean>) HostBean.listAll(HostBean.class);
            ArrayList<HostBean> newList = new ArrayList<>();
            int index = list1.size();
            for (int i=0; i< index; i++){
                if(list1.get(i).onlineStatus){
                    newList.add(0, list1.get(i));
                    list1.remove(list1.get(i));
                    index = index - 1;
                    i = i - 1;
                }
            }
            for(HostBean hostBean: newList){
                list1.add(0, hostBean);
            }

            hostBeansForMinglers = list1;
            setIndicationMessage();
            Intent intent = new Intent(INTENT_FILTER_BROADCAST);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("host_list", hostBeansForMinglers);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(ctxt).sendBroadcast(intent);

            ArrayList<HostBean> listt = (ArrayList<HostBean>) HostBean.listAll(HostBean.class);
            if(list.size() > 0){
                for (HostBean h: listt){
                    h.status = OFFLINE;
                    h.onlineStatus = false;
                    HostBean.save(h);
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    TabActivity.isScanStart = false;
                    initDiscovery();
                }
            }, sharedPreferences.getInt("timerForScan", 5000));
        }
    }

    public void setHostBeanListOthers(ArrayList<HostBean> list){
        if(checkWifiStatus()) {
            hostBeansForOthers = list;
            Intent intent = new Intent(INTENT_FILTER_BROADCAST_OTHERS);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("host_list", hostBeansForOthers);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(ctxt).sendBroadcast(intent);
        }
    }

    public ArrayList<HostBean> checkDuplicate(ArrayList<HostBean> list) {
        ArrayList<HostBean> listNew = new ArrayList<>();
        for (HostBean host : list) {
            boolean isFound = false;
            for (HostBean hostBean : listNew) {
                if (hostBean.ipAddress.equals(host.ipAddress)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) listNew.add(host);
        }
        return listNew;
    }

    private boolean checkWifiStatus(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connMgr.getActiveNetworkInfo();
        if(ni != null){
            if(ni.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)){
                if(ni.getType() == ConnectivityManager.TYPE_WIFI){
                    return true;
                }
            }else {
                return false;
            }
        }else {
            return false;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.registeration_menu, menu);

        MenuItem menu1 = menu.findItem(R.id.menu_item_action_parameters);
        MenuItem allMenu = menu.findItem(R.id.action_dropdown3);
        MenuItem availableMenu = menu.findItem(R.id.action_dropdown1);
        MenuItem busyMenu = menu.findItem(R.id.action_dropdown2);
        allMenu.setChecked(true);

        if(tabLayout.getSelectedTabPosition() == 1){
            menu1.setVisible(false);
        }else {
            menu1.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(INTENT_FILTER_BROADCAST);
        Bundle bundle = new Bundle();
        switch (id) {
            case R.id.menu_item_action_parameters:
                // What to do here?
                break;

            case R.id.menu_settings:
                startActivity(new Intent(TabActivity.this, ActivitySettings.class));
                break;
            case R.id.action_dropdown1:
                if(hostBeansForMinglers != null) {
                    item.setChecked(true);
                    FILTER = AVAILABLE;
                    bundle.putParcelableArrayList("host_list", hostBeansForMinglers);
                    intent.putExtras(bundle);
                    LocalBroadcastManager.getInstance(ctxt).sendBroadcast(intent);
                }
                break;

            case R.id.action_dropdown2:
                if(hostBeansForMinglers != null) {
                    item.setChecked(true);
                    FILTER = BUSY;
                    bundle.putParcelableArrayList("host_list", hostBeansForMinglers);
                    intent.putExtras(bundle);
                    LocalBroadcastManager.getInstance(ctxt).sendBroadcast(intent);
                }
                break;

            case R.id.action_dropdown3:
                if(hostBeansForMinglers != null) {
                    item.setChecked(true);
                    FILTER = ALL;
                    bundle.putParcelableArrayList("host_list", hostBeansForMinglers);
                    intent.putExtras(bundle);
                    LocalBroadcastManager.getInstance(ctxt).sendBroadcast(intent);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}