package com.wifimingle.interfaces;

import com.wifimingle.model.FeedBackModel;
import com.wifimingle.model.RegistrationModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Saad on 2/26/2018.
 */

public interface ApiService {
    //@POST("/posts")
    @POST("feedback/save")
    @FormUrlEncoded
    Call<FeedBackModel> savePost(@Field("fb_name") String fbName,
                                 @Field("fb_email") String fbEmail,
                                 @Field("fb_feedback") String feedBack,
                                 @Field("fb_image") String fbImage,
                                 @Field("api_key") String apiKey);

    @POST("api/user_registeration")
    @FormUrlEncoded
    Call<RegistrationModel> saveRegistrationPost(@Field("name") String rgName,
                                                 @Field("phone_number") String rgPhone,
                                                 @Field("gender") String rgGender,
                                                 @Field("birthday") String rgBirthday,
                                                 @Field("api_key") String apiKey,
                                                 @Field("imei_no") String rgImei,
                                                 @Field("user_image") String rgProfilePic);
}
