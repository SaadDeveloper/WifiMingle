package com.wifimingle.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

/**
 * Created by BrOlLy on 23/02/2018.
 */

public class FeedBackModel extends SugarRecord {

    @SerializedName("fb_name")
    @Expose
    public String fbName;

    @SerializedName("api_key")
    @Expose
    public String fbApiKey;

    @SerializedName("fb_email")
    @Expose
    public String fbEmail;

    @SerializedName("fb_feedback")
    @Expose
    public String fbComplaint_feedback;

    @SerializedName("fb_image")
    @Expose
    public String fbImageString;

    public String getFbName() {
        return fbName;
    }

    public void setFbName(String fbName) {
        this.fbName = fbName;
    }

    public String getFbEmail() {
        return fbEmail;
    }

    public void setFbEmail(String fbEmail) {
        this.fbEmail = fbEmail;
    }

    public String getFbComplaint_feedback() {
        return fbComplaint_feedback;
    }

    public void setFbComplaint_feedback(String fbComplaint_feedback) {
        this.fbComplaint_feedback = fbComplaint_feedback;
    }

    public String getFbApiKey() {
        return fbApiKey;
    }

    public void setFbApiKey(String fbApiKey) {
        this.fbApiKey = fbApiKey;
    }

    public String getFbImageString() {
        return fbImageString;
    }

    public void setFbImageString(String fbImageString) {
        this.fbImageString = fbImageString;
    }
}
