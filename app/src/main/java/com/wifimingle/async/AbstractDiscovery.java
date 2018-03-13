package com.wifimingle.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;

import com.google.gson.Gson;
import com.wifimingle.R;
import com.wifimingle.activity.BaseActivity;
import com.wifimingle.activity.TabActivity;
import com.wifimingle.model.HostBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.content.Context.WIFI_SERVICE;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST;
import static com.wifimingle.activity.ActivityMain.INTENT_FILTER_BROADCAST_OTHERS;

public abstract class AbstractDiscovery extends AsyncTask<Void, HostBean, Void> {

    protected int hosts_done = 0;
    private ArrayList<HostBean> hostBeans;
    private ArrayList<HostBean> hostBeansForOthers;
    private ArrayList<HostBean> hostBeansForDb;
    private ProgressDialog progressDialog;
    public WeakReference<BaseActivity> mDiscover;
    private Activity activity;

    protected long ip;
    protected long start = 0;
    protected long end = 0;
    protected long size = 0;

    public AbstractDiscovery(Activity activity) {
        mDiscover = new WeakReference<BaseActivity>((TabActivity) activity);
        this.activity = activity;
    }

    public void setNetwork(long ip, long start, long end) {
        this.ip = ip;
        this.start = start;
        this.end = end;
    }

    abstract protected Void doInBackground(Void... params);

    @Override
    protected void onPreExecute() {
        TabActivity.isScanStart = true;
        progressDialog = new ProgressDialog(activity);
        hostBeans = new ArrayList<>();
        hostBeansForOthers = new ArrayList<>();
        hostBeansForDb = new ArrayList<>();
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Fetching devices");
        progressDialog.setMax(100);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        size = (int) (end - start + 1);
    }

    @Override
    protected void onProgressUpdate(HostBean... host) {
        if (mDiscover != null) {
            final BaseActivity discover = mDiscover.get();
            if (discover != null) {
                if (!isCancelled()) {

                    if (host[0] != null) {
                        if (!getLocalIpAddress().equals(host[0].ipAddress)) {
                            if (host[0].deviceName != null) {
                                hostBeans.add(host[0]);
                                sendBroadCastToMinglerFragment(activity, host[0]);
                            } else {
                                hostBeansForOthers.add(host[0]);
                                sendBroadCastToOthersFragment(activity, host[0]);
                            }
                        }
                    }
                    if (size > 0) {
                        ((TabActivity) activity).setFetchedMinglersProgress((int) (hosts_done * 100 / size) + "% Sniffed");
                        progressDialog.setMessage("Please Wait.... " + (int) (hosts_done * 100 / size) + "% Loaded");
                    }
                }
            }
        }

    }

    @Override
    protected void onPostExecute(Void unused) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (activity instanceof TabActivity) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((TabActivity) activity).setHostBeanListMinglers(hostBeans);
                    ((TabActivity) activity).setHostBeanListOthers(hostBeansForOthers);
                    ((TabActivity) activity).setFetchedMinglersProgress(null);
                }
            }, 1000);
        }
    }

    private void sendBroadCastToMinglerFragment(Context context, HostBean hostBean) {
        Intent intent = new Intent(INTENT_FILTER_BROADCAST);
        String hostString = new Gson().toJson(hostBean);
        intent.putExtra("host", hostString);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendBroadCastToOthersFragment(Context context, HostBean hostBean) {
        Intent intent = new Intent(INTENT_FILTER_BROADCAST_OTHERS);
        String hostString = new Gson().toJson(hostBean);
        intent.putExtra("host", hostString);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    protected void onCancelled() {
        if (mDiscover != null) {
            final BaseActivity discover = mDiscover.get();
            if (discover != null) {
                discover.makeToast(R.string.discover_canceled);
            }
        }
    }

    public String getLocalIpAddress() {
        WifiManager wm = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}
