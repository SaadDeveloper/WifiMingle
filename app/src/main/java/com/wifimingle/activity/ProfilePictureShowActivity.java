package com.wifimingle.activity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.wifimingle.R;

import java.io.File;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
import static com.wifimingle.activity.ActivityMain.MY_PREFERENCES;

public class ProfilePictureShowActivity extends AppCompatActivity {

    //private ImageView profilePic;
    private PhotoView profilePic;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean show_hide_flag = true;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeNavigationTransparent();
        setContentView(R.layout.activity_profile_picture_show);

        initAndPopulate();
        //setStatusBarColour();
        setActionBar();
        initListeners();
    }

    private void initAndPopulate() {
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        profilePic = findViewById(R.id.picture);
        try {
            String imgString = sharedPreferences.getString("image", "");
            name = sharedPreferences.getString("name", "");
            Uri uri = Uri.fromFile(new File(imgString));
            //byte[] imgByte = convertToByteArray(imgString);
            //Bitmap image = getImage(imgByte);
            Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            profilePic.setImageBitmap(image);
            editor.putString("image", "");
            editor.putString("name", "");
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListeners() {
        /*profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (show_hide_flag) {
                    hideNavigationAndStatus();
                    show_hide_flag = false;
                }
                *//*if(show_hide_flag) {
                    //hideHeading();
                    hideNavigationAndStatus();
                    show_hide_flag = false;
                }else {
                    showHeading();
                    show_hide_flag = true;
                }*//*
                //hideNavigationAndStatus();
            }
        });*/
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private void hideNavigationAndStatus() {
        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions1 = SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions1);
        getSupportActionBar().hide();
        /*if(getSupportActionBar().isShowing()) {
            int uiOptions = SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            getSupportActionBar().hide();
        }else {
            //this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            int uiOptions = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
            getSupportActionBar().show();
        }*/
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (show_hide_flag) {
            setScreenMode(true);
            //hideNavigationAndStatus();
            show_hide_flag = false;
        }else {
            setScreenMode(false);
            show_hide_flag = true;
        }
    }

    private void setScreenMode(boolean fullscreen) {
        if (fullscreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            //decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            getSupportActionBar().hide();
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getSupportActionBar().show();
        }

    }

    private void makeNavigationTransparent() {
        View decorView = getWindow().getDecorView();

        int uiOptions = SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void hideHeading() {
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);
        getSupportActionBar().hide();
    }

    private void showHeading() {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().show();
    }

    private void setStatusBarColour() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }
    }

    public Bitmap getImage(byte[] image) throws Exception {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public byte[] convertToByteArray(String base64String) {
        return Base64.decode(base64String, Base64.DEFAULT);
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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
