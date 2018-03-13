package com.wifimingle.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wifimingle.R;
import com.wifimingle.Utils.RoundCornersImageView;
import com.wifimingle.activity.ProfilePictureShowActivity;
import com.wifimingle.model.ChatMessageModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.wifimingle.activity.ActivityMain.MY_PREFERENCES;
import static com.wifimingle.activity.ActivitySingleChat.SEEN;
import static com.wifimingle.activity.ActivitySingleChat.UNSEEN;

public class SingleChatListAdapter extends BaseAdapter {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Activity activity;
    private Context context;
    private ArrayList<ChatMessageModel> messages;
    private LayoutInflater layoutInflater;
    private String userName;

    public SingleChatListAdapter(Context context, Activity activity, ArrayList<ChatMessageModel> messageList, String name) {
        this.context = context;
        this.activity = activity;
        userName = name;
        layoutInflater = activity.getLayoutInflater();
        messages = messageList;
        sharedPreferences = activity.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        final ChatMessageModel message = messages.get(i);

        int res;
        if (message.from_client_server.equalsIgnoreCase("server")) {
            res = R.layout.message_left;
        } else {
            res = R.layout.message_right;
        }

        convertView = layoutInflater.inflate(res, viewGroup, false);
        LinearLayout layout = convertView.findViewById(R.id.bubble);
        TextView txtMessage = convertView.findViewById(R.id.in_1);
        RoundCornersImageView imageMessage = convertView.findViewById(R.id.iv_image_message);
        TextView time = convertView.findViewById(R.id.date_time);
        ImageView seen_unseen = convertView.findViewById(R.id.seen_unseen);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!message.onlineStatus) {
                layout.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.colorAccent));
            }
        }
        if(message.seen_unseen.equals(SEEN)){
            seen_unseen.setImageResource(R.drawable.tick_blue);
        }else if(message.seen_unseen.equals(UNSEEN)) {
            seen_unseen.setImageResource(R.drawable.tick_grey);
        }

        if(message.imagePath != null && !message.imagePath.equals("")) {
            imageMessage.setVisibility(View.VISIBLE);
            Uri uri = Uri.fromFile(new File(message.imagePath));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                imageMessage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            imageMessage.setVisibility(View.GONE);
        }

        if(!message.chatMessage.equals("")) {
            txtMessage.setText(message.chatMessage);
        }else {
            txtMessage.setVisibility(View.GONE);
        }

        imageMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ProfilePictureShowActivity.class);
                editor.putString("image", message.imagePath);
                editor.putString("name", userName);
                editor.apply();
                activity.startActivity(intent);
            }
        });
        time.setText(message.date);

        return convertView;
    }

    public void setUpdatedList(ArrayList<ChatMessageModel> list, String name){
        userName = name;
        messages = list;
    }
}

