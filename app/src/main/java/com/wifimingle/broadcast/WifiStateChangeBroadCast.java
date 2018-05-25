package com.wifimingle.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import com.wifimingle.Utils.Utilities;
import com.wifimingle.activity.RegistrationActivity;
import com.wifimingle.activity.SplashActivity;
import com.wifimingle.application.BaseApplication;
import com.wifimingle.interfaces.ApiService;
import com.wifimingle.model.FeedBackModel;
import com.wifimingle.model.RegistrationModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wifimingle.Utils.Utilities.getIMEINumber;
import static com.wifimingle.constants.Constants.API_KEY;

public class WifiStateChangeBroadCast extends BroadcastReceiver {

    private ApiService mApiService;

    @Override
    public void onReceive(Context context, Intent intent) {
        mApiService = BaseApplication.retrofit.create(ApiService.class);
        List<FeedBackModel> mFeedbackModels = fetchLocallySavedData();
        RegistrationModel registrationModel = fetchLocalRegisteredUser();
        /*byte[] bytes = Base64.decode(extra, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);*/
        if(Utilities.isNetworkAvailable(context)) {
            if(mFeedbackModels.size() > 0){
                for (FeedBackModel feedBackModel: mFeedbackModels) {
                    sendPost(feedBackModel, feedBackModel.getFbName(), feedBackModel.getFbEmail(), feedBackModel.getFbComplaint_feedback(), feedBackModel.getFbImageString());
                }
            }
            if(!registrationModel.sent){
                sendPost(registrationModel.getName(), registrationModel.getGender(), registrationModel.getDob(), registrationModel.getPhone(),
                        registrationModel.getProfilePicString(), registrationModel.getImeiNo(), context);
            }
        }
    }

    private List<FeedBackModel> fetchLocallySavedData() {
        return FeedBackModel.listAll(FeedBackModel.class);
    }

    private RegistrationModel fetchLocalRegisteredUser(){
        return RegistrationModel.first(RegistrationModel.class);
    }

    public void sendPost(final FeedBackModel feedBackModel, String fbName, String fbEmail, String fbMessage, String fbImage) {
        mApiService.savePost(fbName, fbEmail, fbMessage, fbImage, API_KEY).enqueue(new Callback<FeedBackModel>() {
            @Override
            public void onResponse(Call<FeedBackModel> call, Response<FeedBackModel> response) {
                if (response.message().equals("OK")) {
                    FeedBackModel.deleteAll(FeedBackModel.class, "id = ?", String.valueOf(feedBackModel.getId()));
                }
            }

            @Override
            public void onFailure(Call<FeedBackModel> call, Throwable t) {
            }
        });
    }

    public void sendPost(String rgName, String rgGender, String rgDob, String rgPhone, String rgImage, String imei, Context context) {
        mApiService.saveRegistrationPost(rgName, rgPhone, rgGender, rgDob, API_KEY, imei, rgImage).enqueue(new Callback<RegistrationModel>() {
            @Override
            public void onResponse(Call<RegistrationModel> call, Response<RegistrationModel> response) {
                if (response.message().equals("OK")){
                    RegistrationModel registrationModel = RegistrationModel.first(RegistrationModel.class);
                    registrationModel.sent = true;
                    registrationModel.setProfilePicString("");
                    registrationModel.save();
                }
            }

            @Override
            public void onFailure(Call<RegistrationModel> call, Throwable t) {
            }
        });
    }
}
