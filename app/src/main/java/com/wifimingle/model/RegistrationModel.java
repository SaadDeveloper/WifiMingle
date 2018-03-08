package com.wifimingle.model;

import com.orm.SugarRecord;

import static com.wifimingle.activity.ActivityMain.AVAILABLE;

/**
 * Created by BrOlLy on 05/12/2017.
 */

public class RegistrationModel extends SugarRecord {

    public String name;
    public String phone;
    public String gender;
    public String dob;
    public String status = AVAILABLE;
    public byte[] profilePic;

    public RegistrationModel() {
    }
}
