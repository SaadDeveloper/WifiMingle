package com.wifimingle.interfaces;

import com.wifimingle.model.FeedBackModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Saad on 2/26/2018.
 */

public interface ApiService {
    //@POST("/posts")
    @POST("wifimingle/feedback/save")
    @FormUrlEncoded
    Call<FeedBackModel> savePost(@Field("fb_name") String fbName,
                                 @Field("fb_email") String fbEmail,
                                 @Field("fb_feedback") String feedBack,
                                 @Field("fb_image") String fbImage,
                                 @Field("api_key") String apiKey);
}
