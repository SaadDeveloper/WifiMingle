package com.wifimingle.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;
import com.wifimingle.R;
import com.wifimingle.Utils.DialogDatePicker;
import com.wifimingle.model.RegistrationModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.wifimingle.Utils.Utilities.getCurrentTimeStamp;
import static com.wifimingle.activity.ActivityMain.FORMAT_DATE;
import static com.wifimingle.activity.ActivityMain.MY_PREFERENCES;
import static com.wifimingle.constants.Constants.APP_MINGLER_IMAGE_FOLDER;
import static com.wifimingle.constants.Constants.APP_MINGLER_IMAGE_SENT_FOLDER;
import static com.wifimingle.constants.Constants.APP_NAME;
import static com.wifimingle.constants.Constants.APP_REGISTERATION_FOLDER;
import static com.wifimingle.constants.Constants.CAMERA;
import static com.wifimingle.constants.Constants.DATE_TIME_24HOUR_FORMATE;
import static com.wifimingle.constants.Constants.DATE_TIME_FORMATE_IMG_SAVING;
import static com.wifimingle.constants.Constants.STORAGE;

public class RegistrationActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String APP_TAG = "RegistrationPage";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private EditText et_name;
    private EditText et_dob;
    private Spinner spGender;
    private CountryCodePicker cpp_code;
    private EditText et_phone;
    private Button btnRegister;
    private CircleImageView profilePic;
    public DatePicker mDatePicker;
    private String[] genderArr = {"Gender", "Male", "Female"};
    public RegistrationModel registrationModel;
    private final int CAMERA_REQUEST = 1888;
    //private String imagePath = null;
    private byte[] imgByte = null;
    private Bitmap originalSizeImage = null;
    private  byte[] originalSizeImageByteArr = null;
    private byte[] orginalSizeImageWithoutCropByteArr = null;
    private String originalSizeImageString = null;
    //private byte[] imageByteArr = null;
    private final int PERMISSION_REQUEST_CODE = 1;
    private final int STORAGE_PERMISSION_REQUEST_CODE = 2;
    private File photoFile;
    private String photoPath;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.dark_gray));
            //Utilities.statusBarLightMode(this);
        }
        setContentView(R.layout.activity_registration);

        init();
        listeners();
    }

    private void init() {
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        et_name = findViewById(R.id.etname);
        et_dob = findViewById(R.id.etDOB);
        cpp_code = findViewById(R.id.ccp);
        et_phone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        spGender = findViewById(R.id.spGender);
        profilePic = findViewById(R.id.profile_image);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, genderArr);
        spGender.setAdapter(adapter);

        spGender.setSelection(0, true);
        View v = spGender.getSelectedView();
        ((TextView) v).setTextColor(getResources().getColor(R.color.white));
    }

    private void listeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });

        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spGender.setSelection(position, true);
                View v = spGender.getSelectedView();
                ((TextView) v).setTextColor(getResources().getColor(R.color.white));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        et_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    showDatePickerForMarshmallowAndAbove(RegistrationActivity.this, et_dob);
                } else {
                    DialogDatePicker dataPicker = new DialogDatePicker(RegistrationActivity.this, et_dob, "Select Date of Birth");
                    mDatePicker = dataPicker.showDateDialog();
                }
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*RegistrationModel registrationModel = RegistrationModel.first(RegistrationModel.class);
                if (registrationModel == null || registrationModel.profilePic == null) {
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera, CAMERA_REQUEST);
                }*/
                /*if(imgByte == null){
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera, CAMERA_REQUEST);
                }else {
                    showDialogPicture(RegistrationActivity.this, imgByte, "Profile Picture");
                }*/
                if(originalSizeImage == null){
                    onLaunchCamera();
                }else {
                    showDialogPicture(RegistrationActivity.this, originalSizeImageByteArr, "Profile Picture");
                }
            }
        });

        profilePic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLaunchCamera();
                return true;
            }
        });
    }

    private void validation() {
        /*if(imgByte == null){
            Toast.makeText(getApplicationContext(), "Profile pic not taken \n Click on the avatar icon to open camera", Toast.LENGTH_SHORT).show();
        }else */
        if (!et_name.getText().toString().equals("") && !et_phone.getText().toString().equals("") && !et_dob.getText().toString().equals("")
                && !spGender.getSelectedItem().toString().equals(spGender.getItemAtPosition(0))) {
            registrationModel = new RegistrationModel();
            registrationModel.name = et_name.getText().toString();
            registrationModel.gender = spGender.getSelectedItem().toString();
            registrationModel.dob = et_dob.getText().toString();
            registrationModel.phone = cpp_code.getSelectedCountryCodeWithPlus() + et_phone.getText().toString();
            registrationModel.profilePic = imgByte;
            registrationModel.save();
            startActivity(new Intent(RegistrationActivity.this, SplashActivity.class));
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Fields are empty", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showDatePickerForMarshmallowAndAbove(Activity activity, final EditText editText) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_datepicker, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.dialog_date_datePicker);
        datePicker.setMaxDate(System.currentTimeMillis() - 1000);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.app.AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("Select Date of Birth");
        builder.setView(view);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dob = pad(datePicker.getYear()) + "-" + pad((datePicker.getMonth() + 1)) + "-" + pad(datePicker.getDayOfMonth());
                editText.setText(dob);
            }

            private String pad(int c) {
                if (c >= 10)
                    return String.valueOf(c);
                else
                    return "0" + String.valueOf(c);
            }
        });
        builder.setNegativeButton("cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            try {
                //Bitmap imageBitmap = (Bitmap) extras.get("data");
                //byte[] image = ImageUtils.getInstance().getSimpleBytes(imageBitmap);
                Bitmap PictureTaken = (Bitmap) extras.get("data");
                profilePic.setImageBitmap(PictureTaken);
                imgByte = getBytes(PictureTaken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            try {
                originalSizeImage = BitmapFactory.decodeFile(photoPath);
                originalSizeImage = preventRotation(originalSizeImage, photoPath);

                profilePic.setImageBitmap(originalSizeImage);
                originalSizeImageByteArr = getBytes(originalSizeImage);

                /*orginalSizeImageWithoutCropByteArr = getBytesForNormalSize(originalSizeImage);
                originalSizeImageString = encodeToBase64String(orginalSizeImageWithoutCropByteArr);*/
                /*File actualImage = new File(photoPath);
                File compressedImage = new Compressor(RegistrationActivity.this)
                        .setMaxWidth(186)
                        .setMaxHeight(248)
                        .setQuality(40)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(actualImage);*/
                Bitmap imageBitmap = ThumbnailUtils.extractThumbnail(originalSizeImage, 186, 248);
                imgByte = getLowQualityImageBytes(imageBitmap);
                /*Uri uri = Uri.fromFile(compressedImage);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);*/
                //imgByte = getBytes(bitmap);
            }catch (OutOfMemoryError e) {
                e.printStackTrace();
                showToast("Error loading Image, Out of Memory");
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error loading Image");
            }
        }
    }

    public void onLaunchCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            methodRequiresPermission();
        }else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = getPhotoFileUri();
            Uri fileProvider = FileProvider.getUriForFile(this, "com.wifimingle.camerademo", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    public File getPhotoFileUri() {
        if (isExternalStorageAvailable()) {
            //File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME, APP_REGISTERATION_FOLDER);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(APP_TAG, "failed to create directory");
            }

            photoPath = mediaStorageDir.getPath() + File.separator + "IMG-" + getCurrentTimeStamp() + ".jpeg";
            return new File(photoPath);
        }
        return null;
    }

    private void makeExternalDirectoryForMingle(){
        File f = new File(Environment.getExternalStorageDirectory(), APP_NAME);
        if (!f.exists()) {
            /*if (f.isDirectory()) {
                String[] children = f.list();
                for (String aChildren : children) {
                    File file = new File(f, aChildren);

                    if(file.isDirectory()){
                        String[] innerChild = file.list();
                        for (String s: innerChild){
                            new File(file, s).delete();
                        }
                    }
                    new File(f, aChildren).delete();
                }
            }
            f.delete();*/
            f.mkdirs();
        }

        File f1 = new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME, APP_REGISTERATION_FOLDER);
        if (!f1.exists()) {
            f1.mkdirs();
        }

        File f2 = new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME, APP_MINGLER_IMAGE_FOLDER);
        if (!f2.exists()) {
            f2.mkdirs();
        }

        File f3 = new File(Environment.getExternalStorageDirectory() + "/" + APP_NAME + "/" + APP_MINGLER_IMAGE_FOLDER, APP_MINGLER_IMAGE_SENT_FOLDER);
        if (!f3.exists()) {
            f3.mkdirs();
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    private Bitmap preventRotation(Bitmap bitmap, String photoPath) {
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            return rotatedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public byte[] getBytes(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.5), (int)(bitmap.getHeight() * 0.5), true);//createBitmap(bitmap, 0, 0, 500, 700, matrix, true);

        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system

        Canvas cs = new Canvas(resizedBitmap);
        Paint tPaint = new Paint();
        tPaint.setTextSize(27);
        tPaint.setColor(Color.RED);
        tPaint.setStyle(Paint.Style.FILL);
        cs.drawText(dateTime, resizedBitmap.getWidth(), resizedBitmap.getHeight(), tPaint);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public byte[] getLowQualityImageBytes(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 2), (int)(bitmap.getHeight() * 2), true);//createBitmap(bitmap, 0, 0, 500, 700, matrix, true);

        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE);
        String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system

        Canvas cs = new Canvas(resizedBitmap);
        Paint tPaint = new Paint();
        tPaint.setTextSize(27);
        tPaint.setColor(Color.RED);
        tPaint.setStyle(Paint.Style.FILL);
        cs.drawText(dateTime, resizedBitmap.getWidth(), resizedBitmap.getHeight(), tPaint);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
        return stream.toByteArray();
    }

    public void showDialogPicture(Activity mActivity, Bitmap image, String title) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            LayoutInflater inflater = mActivity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.image_detail, null);
            //View titleView = inflater.inflate(R.layout.custom_dialog_title, null);
            ImageView imageView = dialogView.findViewById(R.id.iv_pic);
            TextView tv_title = dialogView.findViewById(R.id.custom_title);
            if (title != null && title.length() > 0) {
                //builder.setTitle(title);
                tv_title.setText(title);
            }

            /*imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegistrationActivity.this, ProfilePictureShowActivity.class);
                    editor.putString("image", originalSizeImageString);
                    editor.apply();
                    startActivity(intent);
                }
            });*/

            //image = Bitmap.createScaledBitmap(image, 700, 700, true);
            imageView.setImageBitmap(image);
            AlertDialog alertDialog = builder.create();
            alertDialog.setView(dialogView);
            alertDialog.show();
            int width = getResources().getDimensionPixelSize(R.dimen._230sdp);
            int height = getResources().getDimensionPixelSize(R.dimen._270sdp);
            alertDialog.getWindow().setLayout(width, height);
           // alertDialog.getWindow().setLayout(900, 1100);
            /*Dialog dialog = new Dialog(mActivity,
                    android.R.style.Theme_Translucent_NoTitleBar);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.image_detail);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = 200;
            lp.height = 200;
            lp.gravity = Gravity.CENTER;

            //dialog.getWindow().setAttributes(lp);
            dialog.setTitle(title);

            ImageView imageView = (ImageView) dialog.findViewById(R.id.iv_pic);
            imageView.setImageBitmap(image);
            dialog.show();
            dialog.getWindow().setAttributes(lp);*/
        } catch (Exception e) {
            Toast.makeText(mActivity, "Error displaying image", Toast.LENGTH_LONG).show();
        }
    }

    public void showDialogPicture(Activity mActivity, byte[] bsImage, String title) {
        try {
            showDialogPicture(mActivity, getImage(bsImage), title);
        } catch (Exception e) {
            Toast.makeText(mActivity, "Unable to Load Image", Toast.LENGTH_LONG).show();
        }
    }

    public Bitmap getImage(byte[] image) throws Exception {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public void showToast(String messageToToast) {
        Toast.makeText(this, messageToToast, Toast.LENGTH_SHORT).show();
    }

    public String encodeToBase64String(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        } else {
            return Base64.encodeToString(data, 0, data.length, Base64.DEFAULT);
        }

    }

    @AfterPermissionGranted(PERMISSION_REQUEST_CODE)
    private void methodRequiresPermission() {
        String[] perms = {CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = getPhotoFileUri();
            Uri fileProvider = FileProvider.getUriForFile(this, "com.wifimingle.camerademo", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        } else {
            EasyPermissions.requestPermissions(this, "Camera Permission is still pending",
                    PERMISSION_REQUEST_CODE, perms);
        }
    }

    @AfterPermissionGranted(STORAGE_PERMISSION_REQUEST_CODE)
    private void methodRequiresStoragePermission() {
        String[] perms = {STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            makeExternalDirectoryForMingle();
        } else {
            EasyPermissions.requestPermissions(this, "Storage Permission is still pending",
                    STORAGE_PERMISSION_REQUEST_CODE, perms);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        methodRequiresStoragePermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri();
        Uri fileProvider = FileProvider.getUriForFile(this, "com.wifimingle.camerademo", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }*/
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            Toast.makeText(RegistrationActivity.this, "Camera permission is required to access Camera", Toast.LENGTH_SHORT).show();
        }else if(requestCode == STORAGE_PERMISSION_REQUEST_CODE){
            Toast.makeText(RegistrationActivity.this, "Storage permission is necessary for Folders Creation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
