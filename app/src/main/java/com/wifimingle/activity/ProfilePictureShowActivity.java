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
        setActionBar();
    }

    private void initAndPopulate() {
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        profilePic = findViewById(R.id.picture);
        try {
            String imgString = sharedPreferences.getString("image", "");
            name = sharedPreferences.getString("name", "");
            try {
                Uri uri = Uri.fromFile(new File(imgString));
                Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                profilePic.setImageBitmap(image);
            }catch (Exception e){
                profilePic.setImageResource(R.drawable.ic_error);
                e.printStackTrace();
            }

            editor.putString("image", "");
            editor.putString("name", "");
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (show_hide_flag) {
            setScreenMode(true);
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
            getSupportActionBar().hide();
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getSupportActionBar().show();
        }
    }

    private void makeNavigationTransparent() {
        View decorView = getWindow().getDecorView();

        int uiOptions = SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
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
