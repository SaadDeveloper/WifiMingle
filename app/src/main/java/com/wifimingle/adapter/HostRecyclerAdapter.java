package com.wifimingle.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wifimingle.Network.NetInfo;
import com.wifimingle.R;
import com.wifimingle.activity.ActivitySingleChat;
import com.wifimingle.model.HostBean;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static com.wifimingle.activity.ActivityMain.AVAILABLE;
import static com.wifimingle.activity.ActivityMain.BUSY;
import static com.wifimingle.activity.ActivityMain.MY_PREFERENCES;
import static com.wifimingle.activity.ActivityMain.OFFLINE;

public class HostRecyclerAdapter extends RecyclerView.Adapter<HostRecyclerAdapter.HostViewHolder> {

    private Activity context;
    private List<HostBean> hosts;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public HostRecyclerAdapter(Activity context, List<HostBean> list) {
        this.context = context;
        hosts = list;
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public HostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_host, parent, false);
        return new HostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HostViewHolder holder, int position) {
        HostBean hostBean = hosts.get(position);
        holder.bindHolder(hostBean);
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    public class HostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView ip;
        private TextView mac_or_personName;
        private TextView vendorName;
        private TextView onlineIcon;
        private TextView busyIcon;
        private TextView offlineIcon;
        private CircleImageView profile;
        private HostBean hostBean;

        private HostViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            onlineIcon = itemView.findViewById(R.id.online_status);
            busyIcon = itemView.findViewById(R.id.busy_status);
            offlineIcon = itemView.findViewById(R.id.offline_status);

            ip = itemView.findViewById(R.id.ip);
            mac_or_personName = itemView.findViewById(R.id.mac_or_name);
            vendorName = itemView.findViewById(R.id.hostname);

            profile = itemView.findViewById(R.id.profile_image_item_list);
        }

        private void bindHolder(HostBean host){
            hostBean = host;
            int count = saveCountOfNewMessage(host.phoneNumber);
            if(host.onlineStatus && host.status.equals(AVAILABLE)){
                if(count > 0) {
                    onlineIcon.setText(String.valueOf(count));
                }else {
                    onlineIcon.setText("");
                }
                onlineIcon.setVisibility(View.VISIBLE);
                busyIcon.setVisibility(View.GONE);
                offlineIcon.setVisibility(View.GONE);
            }else if(host.onlineStatus && host.status.equals(BUSY)){
                onlineIcon.setVisibility(View.GONE);
                if(count > 0) {
                    busyIcon.setText(String.valueOf(count));
                }else {
                    busyIcon.setText("");
                }
                busyIcon.setVisibility(View.VISIBLE);
                offlineIcon.setVisibility(View.GONE);
            }else if(host.onlineStatus){
                onlineIcon.setVisibility(View.GONE);
                busyIcon.setVisibility(View.GONE);
                offlineIcon.setVisibility(View.GONE);
            }else if(!host.onlineStatus && host.status.equals(OFFLINE)){
                onlineIcon.setVisibility(View.GONE);
                busyIcon.setVisibility(View.GONE);
                offlineIcon.setVisibility(View.VISIBLE);
                if(count > 0){
                    offlineIcon.setText(String.valueOf(count));
                }else {
                    offlineIcon.setText("");
                }
            }

            if (host.deviceType == HostBean.TYPE_GATEWAY) {
                profile.setImageResource(R.drawable.avatar);
            } else if (host.isAlive == 1 || !host.hardwareAddress.equals(NetInfo.NOMAC)) {
                profile.setImageResource(R.drawable.avatar);
            } else {
                profile.setImageResource(R.drawable.avatar);
            }

            if(host.deviceName != null){
                mac_or_personName.setText(host.deviceName);
            }else {
                ip.setVisibility(View.VISIBLE);
                vendorName.setVisibility(View.VISIBLE);
                mac_or_personName.setText(host.hardwareAddress);
                ip.setText(host.ipAddress);
                if (host.hostname != null) {
                    vendorName.setText(host.hostname);
                } else {
                    vendorName.setText("UnKnown");
                }
            }

            try {
                if (host.profilePicString != null) {
                    byte[] img = Base64.decode(host.profilePicString, Base64.DEFAULT);
                    profile.setImageBitmap(getImage(img));
                }else if (host.profilePicString.equals("")){
                    profile.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar));
                }else {
                    profile.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            if(hostBean.phoneNumber != null) {
                Intent intent = new Intent(context, ActivitySingleChat.class);
                intent.putExtra("host", hostBean);
                intent.putExtra("host_name", hostBean.deviceName);
                intent.putExtra("host_phone", hostBean.phoneNumber);
                intent.putExtra("host_status", hostBean.onlineStatus);
                context.startActivity(intent);
            }
        }
    }

    private int saveCountOfNewMessage(String phoneNumber){
        return sharedPreferences.getInt(phoneNumber, 0);
        //editor.putInt(phoneNumber, messageCount);
        //editor.apply();
    }

    private Bitmap getImage(byte[] image) throws Exception {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public void setUpdatedList(ArrayList<HostBean> list){
        hosts = list;
    }
}
