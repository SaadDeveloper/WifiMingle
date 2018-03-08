package com.wifimingle.model;

import com.orm.SugarRecord;

/**
 * Created by BrOlLy on 29/12/2017.
 */

public class PhoneModelForWelcomingMessage extends SugarRecord{

    public String phone;

    public PhoneModelForWelcomingMessage() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
