package com.wifimingle.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.wifimingle.R;
import com.wifimingle.Utils.Utilities;
import com.wifimingle.application.BaseApplication;
import com.wifimingle.interfaces.ApiService;
import com.wifimingle.model.FeedBackModel;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wifimingle.constants.Constants.STORAGE;
import static com.wifimingle.constants.Constants.API_KEY;

public class FeedBackActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private final int PERMISSION_REQUEST_CODE = 1;
    private final int REQUEST_IMAGE_OPEN = 1;

    private ActionBar actionBar;
    private ProgressDialog progressDialog;
    private EditText etName;
    private EditText etEmail;
    private EditText etMessage;
    private ImageView ivAttachment;
    private Button btnSubmit;
    private Button btnAttachment;

    private ApiService mApiService;

    private String name;
    private String email;
    private String message;

    private String attachmentString;
    private Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.dark_gray));
        }
        setContentView(R.layout.activity_feed_back);
        init();
        setMenuHomeButton();
        listeners();
    }

    private void init() {
        actionBar = getSupportActionBar();
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setTitle("Submitting Feedback");
        progressDialog.setMessage("Please wait ....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etMessage = findViewById(R.id.et_message);
        ivAttachment = findViewById(R.id.iv_attachment);
        btnSubmit = findViewById(R.id.btn_submit);
        btnAttachment = findViewById(R.id.btn_attachment);

        mApiService = BaseApplication.retrofit.create(ApiService.class);
    }

    private void setMenuHomeButton() {
        drawable = getResources().getDrawable(R.drawable.chat_header);
        actionBar.setBackgroundDrawable(drawable);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    private void listeners() {
        btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    methodRequiresPermission();
                }else {
                    selectImage();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    if (Utilities.isNetworkAvailable(FeedBackActivity.this)) {
                        sendPost(name, email, message, attachmentString);
                        progressDialog.show();
                    } else {
                        saveLocally();
                    }
                }
            }
        });
    }

    private boolean validateForm() {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        message = etMessage.getText().toString().trim();
        if(name.equals("")){
            Toast.makeText(this, "Name field is Empty", Toast.LENGTH_SHORT).show();
            return false;
        }else if(email.equals("")){
            Toast.makeText(this, "Email field is Empty", Toast.LENGTH_SHORT).show();
            return false;
        }else if(message.equals("")){
            Toast.makeText(this, "Feedback field is Empty", Toast.LENGTH_SHORT).show();
            return false;
        }else if(!isValidEmail(email)){
            Toast.makeText(this, "Email is not Valid", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void resetForm(){
        etName.setText("");
        etEmail.setText("");
        etMessage.setText("");
        ivAttachment.setImageDrawable(null);
        ivAttachment.setVisibility(View.GONE);
    }

    public void sendPost(String fbName, String fbEmail, String fbMessage, String fbImage) {
        mApiService.savePost(fbName, fbEmail, fbMessage, fbImage, API_KEY).enqueue(new Callback<FeedBackModel>() {
            @Override
            public void onResponse(Call<FeedBackModel> call, Response<FeedBackModel> response) {
                progressDialog.dismiss();
                if (response.message().equals("OK")) {
                    resetForm();
                    Toast.makeText(FeedBackActivity.this, "Thankyou for your Feedback", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FeedBackActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FeedBackModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(FeedBackActivity.this, "Unable to submit post to API.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocally() {
        long id = getFeedBack().save();
        Toast.makeText(FeedBackActivity.this, "Internet is not Working... Saving Locally", Toast.LENGTH_LONG).show();
    }

    private FeedBackModel getFeedBack() {
        FeedBackModel mFeedBack = new FeedBackModel();
        mFeedBack.setFbName(name);
        mFeedBack.setFbEmail(email);
        mFeedBack.setFbComplaint_feedback(message);
        mFeedBack.setFbImageString(attachmentString);
        return mFeedBack;
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_OPEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_OPEN && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            try {
                if (fullPhotoUri != null) {
                    ivAttachment.setVisibility(View.VISIBLE);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fullPhotoUri);
                    ivAttachment.setImageBitmap(bitmap);
                    byte[] byteArray = Utilities.getBytes(bitmap);
                    attachmentString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    @AfterPermissionGranted(PERMISSION_REQUEST_CODE)
    private void methodRequiresPermission() {
        String[] perms = {STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            selectImage();
        } else {
            EasyPermissions.requestPermissions(this, "Storage Permission is still pending",
                    PERMISSION_REQUEST_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "Permission is Required for Gallery access", Toast.LENGTH_SHORT).show();
    }
}
