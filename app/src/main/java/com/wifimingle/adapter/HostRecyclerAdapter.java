package com.wifimingle.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
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

import static com.wifimingle.activity.ActivityMain.AVAILABLE;
import static com.wifimingle.activity.ActivityMain.BUSY;
import static com.wifimingle.activity.ActivityMain.OFFLINE;

/**
 * Created by BrOlLy on 29/11/2017.
 */

public class HostRecyclerAdapter extends RecyclerView.Adapter<HostRecyclerAdapter.HostViewHolder> {

    private Activity context;
    private List<HostBean> hosts;

    public HostRecyclerAdapter(Activity context, List<HostBean> list) {
        this.context = context;
        hosts = list;
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

        public HostViewHolder(View itemView) {
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

        public void bindHolder(HostBean host){
            hostBean = host;

            if(host.onlineStatus && host.status.equals(AVAILABLE)){
                onlineIcon.setVisibility(View.VISIBLE);
                busyIcon.setVisibility(View.GONE);
                offlineIcon.setVisibility(View.GONE);
            }else if(host.onlineStatus && host.status.equals(BUSY)){
                onlineIcon.setVisibility(View.GONE);
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
                if (host.hostname != null/* && !host.hostname.equals(host.ipAddress)*/) {
                    vendorName.setText(host.hostname/* + " (" + host.ipAddress + ")"*/);
                } else {
                    vendorName.setText("UnKnown");
                }
            }

            try {
                if (host.profilePicByte != null && host.profilePicByte.length > 0) {
                    profile.setImageBitmap(getImage(host.profilePicByte));
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
                //((TabActivity) context).cancelTasks();
                context.startActivity(intent);
            }
            /*if(hostBean.deviceName != null && hostBean.onlineStatus) {
                Intent intent = new Intent(context, ActivitySingleChat.class);
                intent.putExtra("host", hostBean);
                intent.putExtra("host_name", hostBean.deviceName);
                intent.putExtra("host_phone", hostBean.phoneNumber);
                intent.putExtra("host_status", hostBean.onlineStatus);
                context.startActivity(intent);
            }else if(hostBean.status.equals(OFFLINE)){
                Toast.makeText(context, "This person is Offline \n Please wait for updated list", Toast.LENGTH_SHORT).show();
            }*/
        }
    }

    public Bitmap getImage(byte[] image) throws Exception {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public void setUpdatedList(ArrayList<HostBean> list){
        hosts = list;
    }
}
