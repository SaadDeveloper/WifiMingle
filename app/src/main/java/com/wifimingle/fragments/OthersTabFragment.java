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
import com.wifimingle.adapter.HostRecyclerAdapter;
import com.wifimingle.interfaces.FragmentMethodCallingOthers;
import com.wifimingle.model.HostBean;

import java.util.ArrayList;

import static com.wifimingle.activity.ActivityMain.FETCHING;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST_OTHERS;
import static com.wifimingle.activity.ActivityMain.SCREEN_MESSAGE;
import static com.wifimingle.activity.ActivityMain.WIFI_DC;

public class OthersTabFragment extends Fragment implements FragmentMethodCallingOthers{

    private View parentView;
    private RecyclerView recyclerView;
    private TextView noOthers;
    private HostRecyclerAdapter hostRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<HostBean> hostBeans;

    public OthersTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(parentView == null) {
            parentView = inflater.inflate(R.layout.others_tab_fargment, container, false);
            init(parentView);
        }
        return parentView;
    }

    private void init(View view){
        recyclerView = view.findViewById(R.id.others_list);
        noOthers = view.findViewById(R.id.tv_no_others);

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
                    noOthers.setVisibility(View.VISIBLE);
                }
            }
        }, 1500);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        hostRecyclerAdapter = new HostRecyclerAdapter(getActivity(), hostBeans);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(hostRecyclerAdapter);
        hostRecyclerAdapter.notifyDataSetChanged();
    }

    public void setIndication(){
        switch (SCREEN_MESSAGE) {
            case WIFI_DC:
                break;
            case FETCHING:
                noOthers.setVisibility(View.GONE);
                break;
            default:
                noOthers.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mRegisterReciever, new IntentFilter(INTENT_FILTER_BROADCAST_OTHERS));
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
                String hostStringDuringScan = intent.getStringExtra("host");
                if(hostStringDuringScan != null){
                    HostBean host = new Gson().fromJson(hostStringDuringScan, HostBean.class);
                    ArrayList<HostBean> copyListForLoop = new ArrayList<>(hostBeans);
                    if(hostBeans.size() > 0) {
                        boolean newDevice = true;
                        int index = 0;
                        for (HostBean saved : copyListForLoop) {
                            if (saved.ipAddress.equals(host.ipAddress)) {
                                hostBeans.remove(index);
                                hostBeans.add(index, host);
                                newDevice = false;
                                break;
                            }
                            index = index + 1;
                        }
                        if (newDevice) {
                            hostBeans.add(host);
                            hostRecyclerAdapter.setUpdatedList(hostBeans);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    }else {
                        hostBeans.add(host);
                        hostRecyclerAdapter.setUpdatedList(hostBeans);
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                }else {
                    ArrayList<HostBean> list = intent.getParcelableArrayListExtra("host_list");
                    if (hostBeans != null && recyclerView.getAdapter() != null) {
                        hostBeans = list;
                        hostRecyclerAdapter.setUpdatedList(hostBeans);
                        recyclerView.getAdapter().notifyDataSetChanged();
                    } else {
                        hostRecyclerAdapter = new HostRecyclerAdapter(getActivity(), list);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setAdapter(hostRecyclerAdapter);
                        hostRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    public static Fragment newInstance(ArrayList<HostBean> hosts){
        Bundle args = new Bundle();
        args.putParcelableArrayList("parameter", hosts);
        OthersTabFragment fragment = new OthersTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void updateListOthers() {
        setIndication();
    }
}
