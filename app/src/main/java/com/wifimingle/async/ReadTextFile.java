package com.wifimingle.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;

import com.wifimingle.R;
import com.wifimingle.activity.SplashActivity;
import com.wifimingle.model.NicVendorsOffline;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by BrOlLy on 02/02/2018.
 */

public class ReadTextFile extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Activity activity;
    private ArrayList<NicVendorsOffline> nicVendorsOfflines;

    public ReadTextFile(Activity mActivity) {
        activity = mActivity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            InputStream inputStream = activity.getResources().openRawResource(R.raw.nicvendors);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            nicVendorsOfflines = new ArrayList<>();

            String eachline = bufferedReader.readLine();
            while (eachline != null) {
                // `the words in the file are separated by space`, so to get each words
                String[] words = eachline.split("\t");
                if(words.length == 3){
                    NicVendorsOffline nicVendorsOffline = new NicVendorsOffline(words[0], words[1], words[2]);
                    nicVendorsOfflines.add(nicVendorsOffline);
                }else if (words.length == 2){
                    NicVendorsOffline nicVendorsOffline = new NicVendorsOffline(words[0], words[1]);
                    nicVendorsOfflines.add(nicVendorsOffline);
                }
                eachline = bufferedReader.readLine();
            }
            NicVendorsOffline.saveInTx(nicVendorsOfflines);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        ((SplashActivity)activity).startDiscoverActivity();
    }
}
