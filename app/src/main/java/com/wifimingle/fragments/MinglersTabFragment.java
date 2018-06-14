package com.wifimingle.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wifimingle.R;
import com.wifimingle.activity.ActivityMain;
import com.wifimingle.activity.TabActivity;
import com.wifimingle.adapter.HostRecyclerAdapter;
import com.wifimingle.interfaces.FragmentMethodCallingMinglers;
import com.wifimingle.model.HostBean;
import com.wifimingle.model.RegistrationModel;

import java.util.ArrayList;

import static com.wifimingle.activity.ActivityMain.ALL;
import static com.wifimingle.activity.ActivityMain.AVAILABLE;
import static com.wifimingle.activity.ActivityMain.BUSY;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST;
import static com.wifimingle.activity.ActivityMain.NO_MINGLER;
import static com.wifimingle.activity.ActivityMain.OFFLINE;
import static com.wifimingle.activity.ActivityMain.SCREEN_MESSAGE;
import static com.wifimingle.activity.ActivityMain.WIFI_DC;


public class MinglersTabFragment extends Fragment implements FragmentMethodCallingMinglers {

    private View parentView;
    private RecyclerView recyclerView;
    private TextView filerText;
    private LinearLayout progressMingler;
    private TextView fetchingMinglers;
    private HostRecyclerAdapter hostRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<HostBean> hostBeans;

    public MinglersTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initFragment(inflater, container);
    }

    private View initFragment(LayoutInflater inflater, ViewGroup container){
        if(parentView == null) {
            parentView = inflater.inflate(R.layout.minglers_tab_fragment, container, false);
            init(parentView);
        }
        return parentView;
    }

    private void init(View view){
        recyclerView = view.findViewById(R.id.minglers_list);
        filerText = view.findViewById(R.id.tv_filter);
        progressMingler = view.findViewById(R.id.progress_minglers);
        fetchingMinglers = view.findViewById(R.id.fetching_minglers);

        if(getArguments() != null){
            hostBeans = getArguments().getParcelableArrayList("parameter");
            if(hostBeans == null){
                hostBeans = new ArrayList<>();
            }
        }else {
            hostBeans = new ArrayList<>();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(hostBeans.size() == 0){
                    SCREEN_MESSAGE = NO_MINGLER;
                    setIndication();
                }
            }
        }, 1500);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        hostRecyclerAdapter = new HostRecyclerAdapter(getActivity(), hostBeans);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(hostRecyclerAdapter);
        hostRecyclerAdapter.notifyDataSetChanged();
    }

    public void settingMinglerProgress(String progress){
        if(progressMingler != null) {
            progressMingler.setVisibility(View.VISIBLE);
            if (progress != null) {
                fetchingMinglers.setText(progress);
            } else {
                progressMingler.setVisibility(View.GONE);
            }
        }
    }

    public void setIndication(){
        if(SCREEN_MESSAGE.equals(NO_MINGLER)){
            filerText.setVisibility(View.VISIBLE);
        }else if(SCREEN_MESSAGE.equals(WIFI_DC) && hostBeans.size() == 0){
            filerText.setVisibility(View.VISIBLE);
        }else {
            filerText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mRegisterReciever, new IntentFilter(INTENT_FILTER_BROADCAST));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRegisterReciever);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BroadcastReceiver mRegisterReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null){
                String availabilityStatus = intent.getStringExtra("status");
                String hostIpString = intent.getStringExtra("host_ip_String");
                String hostString = intent.getStringExtra("host_String");
                String regString = intent.getStringExtra("reg_String");
                String hostStringDuringScan = intent.getStringExtra("host");

                if(hostStringDuringScan != null) {
                    HostBean host = new Gson().fromJson(hostStringDuringScan, HostBean.class);
                    ArrayList<HostBean> copyListForLoop = new ArrayList<>(hostBeans);
                    if(hostBeans.size() > 0) {
                        boolean newDevice = true;
                        int index = 0;
                        for (HostBean saved : copyListForLoop) {
                            if (saved.phoneNumber.equals(host.phoneNumber)) {
                                hostBeans.remove(index);
                                hostBeans.add(index, host);
                                newDevice = false;
                                break;
                            }
                            index = index + 1;
                        }
                        if (newDevice) {
                            hostBeans.add(host);
                        }

                        ArrayList<HostBean> newList = new ArrayList<>();
                        int index1 = hostBeans.size();
                        for (int i = 0; i < index1; i++) {
                            if (hostBeans.get(i).onlineStatus) {
                                newList.add(0, hostBeans.get(i));
                                hostBeans.remove(hostBeans.get(i));
                                index1 = index1 - 1;
                                i = i - 1;
                            }
                        }
                        for (HostBean hostBean1 : newList) {
                            hostBeans.add(0, hostBean1);
                        }
                        populateListAccordingToFilter(hostBeans);
                    }else {
                        hostBeans.add(host);
                        hostRecyclerAdapter.setUpdatedList(hostBeans);
                        recyclerView.getAdapter().notifyDataSetChanged();

                        populateListAccordingToFilter(hostBeans);
                    }
                }else if(availabilityStatus != null){
                    if(hostBeans.size() > 0){
                        int count = 0;
                        for (HostBean host1: hostBeans){
                            if(host1.ipAddress.equals(hostIpString) && host1.onlineStatus){
                                host1.status = availabilityStatus;
                                hostBeans.set(count, host1);
                            }
                            count++;
                        }
                        populateListAccordingToFilter(hostBeans);
                    }
                } else if(hostString != null){
                    HostBean hostBean = new Gson().fromJson(hostString, HostBean.class);
                    RegistrationModel reg = new Gson().fromJson(regString, RegistrationModel.class);
                    hostBean.onlineStatus = true;
                    hostBean.deviceName = reg.name;
                    hostBean.status = reg.status;
                    hostBean.phoneNumber = reg.phone;
                    hostBean.profilePicString = reg.profilePic;
                    ArrayList<HostBean> copyListForLoop1 = new ArrayList<>(hostBeans);
                    if(hostBeans.size() > 0){
                        boolean newDevice = true;
                        int index = 0;
                        for (HostBean saved : copyListForLoop1) {
                            if (saved.phoneNumber.equals(hostBean.phoneNumber)) {
                                hostBeans.remove(index);
                                hostBeans.add(index, hostBean);
                                newDevice = false;
                                break;
                            }
                            index = index + 1;
                        }
                        if (newDevice) {
                            hostBeans.add(hostBean);
                        }

                        ArrayList<HostBean> newList = new ArrayList<>();
                        int index1 = hostBeans.size();
                        for (int i=0; i< index1; i++){
                            if(hostBeans.get(i).onlineStatus){
                                newList.add(0, hostBeans.get(i));
                                hostBeans.remove(hostBeans.get(i));
                                index1 = index1 - 1;
                                i = i - 1;
                            }
                        }
                        for(HostBean hostBean1: newList){
                            hostBeans.add(0, hostBean1);
                        }
                        populateListAccordingToFilter(hostBeans);
                    }else {
                        hostBean.onlineStatus = true;
                        hostBean.deviceName = reg.name;
                        hostBean.status = reg.status;
                        hostBean.phoneNumber = reg.phone;
                        hostBean.profilePicString = reg.profilePic;
                        hostBeans.add(hostBean);
                        hostRecyclerAdapter.setUpdatedList(hostBeans);
                        recyclerView.getAdapter().notifyDataSetChanged();

                        populateListAccordingToFilter(hostBeans);
                    }
                }else {
                    ArrayList<HostBean> list = intent.getParcelableArrayListExtra("host_list");
                    populateListAccordingToFilter(list);
                }
            }
        }
    };

    private void populateListAccordingToFilter(ArrayList<HostBean> hostBeansList){
        if (hostBeans != null && recyclerView.getAdapter() != null) {
            hostBeans = hostBeansList;
            if(TabActivity.FILTER == null || TabActivity.FILTER.equals(ALL)) {
                if (hostBeans.size() == 0) {
                    ActivityMain.SCREEN_MESSAGE = NO_MINGLER;
                } else {
                    ActivityMain.SCREEN_MESSAGE = "";
                }
                setIndication();
                hostRecyclerAdapter.setUpdatedList(hostBeans);
                recyclerView.getAdapter().notifyDataSetChanged();
            } else if (TabActivity.FILTER.equals(AVAILABLE)) {
                ArrayList<HostBean> filterList = new ArrayList<>();
                if (hostBeans.size() > 0) {
                    for (HostBean h : hostBeans) {
                        if (h.status.equals(AVAILABLE)) {
                            filterList.add(h);
                        }else if(h.status.equals(OFFLINE)){
                            filterList.add(h);
                        }
                    }
                }
                if (filterList.size() == 0) {
                    ActivityMain.SCREEN_MESSAGE = NO_MINGLER;
                } else {
                    ActivityMain.SCREEN_MESSAGE = "";
                }
                setIndication();
                hostRecyclerAdapter.setUpdatedList(filterList);
                recyclerView.getAdapter().notifyDataSetChanged();
            } else if (TabActivity.FILTER.equals(BUSY)) {
                ArrayList<HostBean> filterList = new ArrayList<>();
                if (hostBeans.size() > 0) {
                    for (HostBean h : hostBeans) {
                        if (h.status.equals(BUSY)) {
                            filterList.add(h);
                        }else if(h.status.equals(OFFLINE)){
                            filterList.add(h);
                        }
                    }
                }
                if (filterList.size() == 0) {
                    ActivityMain.SCREEN_MESSAGE = NO_MINGLER;
                } else {
                    ActivityMain.SCREEN_MESSAGE = "";
                }
                setIndication();
                hostRecyclerAdapter.setUpdatedList(filterList);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        } else {
            hostRecyclerAdapter = new HostRecyclerAdapter(getActivity(), hostBeansList);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(hostRecyclerAdapter);
            hostRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public static Fragment newInstance(ArrayList<HostBean> hosts){
        Bundle args = new Bundle();
        args.putParcelableArrayList("parameter", hosts);
        MinglersTabFragment fragment = new MinglersTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void updateListMinglers() {
        setIndication();
    }
}
