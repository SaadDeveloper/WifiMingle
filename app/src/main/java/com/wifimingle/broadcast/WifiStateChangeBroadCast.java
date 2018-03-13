package com.wifimingle.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.wifimingle.Utils.Utilities;
import com.wifimingle.application.BaseApplication;
import com.wifimingle.interfaces.ApiService;
import com.wifimingle.model.FeedBackModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.wifimingle.constants.Constants.API_KEY;

public class WifiStateChangeBroadCast extends BroadcastReceiver {

    private ApiService mApiService;
    private List<FeedBackModel> mFeedbackModels = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        mApiService = BaseApplication.retrofit.create(ApiService.class);
        mFeedbackModels = fetchLocallySavedData();
        if(Utilities.isNetworkAvailable(context)) {
            if(mFeedbackModels.size() > 0){
                for (FeedBackModel feedBackModel: mFeedbackModels) {
                    sendPost(feedBackModel, feedBackModel.getFbName(), feedBackModel.getFbEmail(), feedBackModel.getFbComplaint_feedback(), feedBackModel.getFbImageString());
                }
            }
        }
    }

    private List<FeedBackModel> fetchLocallySavedData() {
        return FeedBackModel.listAll(FeedBackModel.class);
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
}
