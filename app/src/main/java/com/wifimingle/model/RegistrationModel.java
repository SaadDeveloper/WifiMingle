package com.wifimingle.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import static com.wifimingle.activity.ActivityMain.AVAILABLE;

/**
 * Created by BrOlLy on 05/12/2017.
 */

public class RegistrationModel extends SugarRecord {

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("phone_number")
    @Expose
    public String phone;

    @SerializedName("gender")
    @Expose
    public String gender;

    @SerializedName("birthday")
    @Expose
    public String dob;

    @SerializedName("api_key")
    @Expose
    public String apiKey;

    @SerializedName("imei_no")
    @Expose
    public String imeiNo;

    @SerializedName("user_image")
    @Expose
    public String profilePicString;
    public String status = AVAILABLE;
    public String profilePic;
    //public byte[] profilePic;
    public boolean sent = false;

    public RegistrationModel() {
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getImeiNo() {
        return imeiNo;
    }

    public void setImeiNo(String imeiNo) {
        this.imeiNo = imeiNo;
    }

    public String getProfilePicString() {
        return profilePicString;
    }

    public void setProfilePicString(String profilePicString) {
        this.profilePicString = profilePicString;
    }
}
