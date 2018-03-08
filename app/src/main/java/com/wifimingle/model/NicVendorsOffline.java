package com.wifimingle.model;

import com.orm.SugarRecord;

/**
 * Created by BrOlLy on 02/02/2018.
 */

public class NicVendorsOffline extends SugarRecord{
    public String macAddress;
    public String companyName;
    public String companyFullName;

    public NicVendorsOffline() {
    }

    public NicVendorsOffline(String macAddress, String companyName, String companyFullName) {
        this.macAddress = macAddress;
        this.companyName = companyName;
        this.companyFullName = companyFullName;
    }

    public NicVendorsOffline(String macAddress, String companyName) {
        this.macAddress = macAddress;
        this.companyName = companyName;
    }
}
